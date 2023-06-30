package com.example.camerel;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class CameraManager {
    private static final String TAG = "CameraManager";
    private final Activity mActivity;
    private final SurfaceView mSurfaceView;
    private final ImageView mImageView;
    //根据需要调整预览和保存的宽高
    private final int PREVIEW_WIDTH = 720;                                         //预览的宽度
    private final int PREVIEW_HEIGHT = 720;                                       //预览的高度
    private int SAVE_WIDTH = 2304;                                            //保存图片的宽度
    private int SAVE_HEIGHT = 4608;                                          //保存图片的高度

    private android.hardware.camera2.CameraManager mCameraManager;
    private ImageReader mImageReader;
    private CameraDevice mCameraDevice;
    private CameraCaptureSession mCameraCaptureSession;
    private String mCameraId = "0";
    private CameraCharacteristics mCameraCharacteristics;

    private int mCameraSensorOrientation = 0;                                           //摄像头方向
    private int mCameraFacing = CameraCharacteristics.LENS_FACING_BACK;                 //默认使用后置摄像头
    private int mDisplayRotation = 0;                                                   //手机方向

    private boolean canExchangeCamera = false;                                               //是否可以切换摄像头

    private Handler mCameraHandler;
    private HandlerThread handlerThread;

    private Size mPreviewSize = new Size(PREVIEW_WIDTH, PREVIEW_HEIGHT);                      //预览大小
    private Size mSavePicSize = new Size(SAVE_WIDTH, SAVE_HEIGHT);                            //保存图片大小
    private final String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };
    public boolean cameraState = false;
    public static String fileName = "cameraTest";

    public CameraManager(Activity mActivity, SurfaceView mSurfaceView, ImageView mImageView) {
        this.mActivity = mActivity;
        this.mSurfaceView = mSurfaceView;
        this.mImageView = mImageView;
        init();
    }

    public void init() {
        handlerThread = new HandlerThread("CameraThread");
        handlerThread.start();
        mCameraHandler = new Handler(handlerThread.getLooper());
        SurfaceHolder holder = mSurfaceView.getHolder();
        holder.setKeepScreenOn(true);
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                initCameraInfo();
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
                releaseCamera();
            }
        });

//        if (mSurfaceView.isAvailable()) {
//
//            Log.d(TAG, "init: texture available");
//        }
//        Log.d(TAG, "init: initListener");
//        mSurfaceView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
//            @Override
//            public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {
//                initCameraInfo();
//                Log.d(TAG, "init: texture available success");
//            }
//
//            @Override
//            public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {
//
//            }
//
//            @Override
//            public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surfaceTexture) {
//                releaseCamera();
//                return true;
//            }
//
//            @Override
//            public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surfaceTexture) {
//
//            }
//
//
//        });
    }

    /**
     * 拍照
     */
    public void takePicture() {
        Log.i(TAG, "takePicture: 进行拍照  1");
        if (mCameraDevice == null ) {
            Log.d(TAG, "takePicture: 进行拍照 error 退出");
            return;
        }
        try {
            CaptureRequest.Builder captureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(mImageReader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            captureBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, mCameraSensorOrientation);
            mCameraCaptureSession.capture(captureBuilder.build(), null, mCameraHandler);
            Thread.sleep(200);
            Log.i(TAG, "takePicture: picture capture finish");
        } catch (CameraAccessException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 切换摄像头
     */
    public void exchangeCamera() {
        if (mCameraDevice == null || !canExchangeCamera ) return;

        mCameraFacing = (mCameraFacing == CameraCharacteristics.LENS_FACING_FRONT)
                ? CameraCharacteristics.LENS_FACING_BACK : CameraCharacteristics.LENS_FACING_FRONT;

        mPreviewSize = new Size(PREVIEW_WIDTH, PREVIEW_HEIGHT);     //重置预览大小
        //重置前后保存图片的尺寸
        if (mCameraFacing == CameraCharacteristics.LENS_FACING_FRONT) {
            SAVE_WIDTH = 1080;
            SAVE_HEIGHT = 1920;
        } else if (mCameraFacing == CameraCharacteristics.LENS_FACING_BACK) {
            SAVE_WIDTH = 4608;
            SAVE_HEIGHT = 2304;
        }
        mSavePicSize = new Size(SAVE_WIDTH, SAVE_HEIGHT);
        releaseCamera();
        initCameraInfo();
    }

    /**
     * 初始化
     */

    private void initCameraInfo() {
        mCameraManager = (android.hardware.camera2.CameraManager) mActivity.getSystemService(Context.CAMERA_SERVICE);
        mDisplayRotation = mActivity.getWindowManager().getDefaultDisplay().getRotation();
        try {
            String[] cameraIdList = mCameraManager.getCameraIdList();
            if (cameraIdList.length == 0) {
                Toast.makeText(mActivity, "没有可用相机", Toast.LENGTH_SHORT).show();
                return;
            }

            for (String id : cameraIdList) {
                CameraCharacteristics cameraCharacteristics = mCameraManager.getCameraCharacteristics(id);
                int facing = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING);

                if (facing == mCameraFacing) {
                    mCameraId = id;
                    mCameraCharacteristics = cameraCharacteristics;
                    break;
                }
                Log.d(TAG, "设备中的摄像头 " + id);
            }
            Log.d(TAG, "initCameraInfo: cameraId : "+mCameraId);
//            int supportLevel = mCameraCharacteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
//            if (supportLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY) {
//            mActivity.toast("相机硬件不支持新特性")
//            }

            //获取摄像头方向
            mCameraSensorOrientation = mCameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            Log.d(TAG, String.valueOf(mCameraSensorOrientation));
            //获取StreamConfigurationMap，它是管理摄像头支持的所有输出格式和尺寸
            StreamConfigurationMap configurationMap = mCameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            Size[] savePicSize = null;
            Size[] previewSize = null;
            if (configurationMap != null) {
                savePicSize = configurationMap.getOutputSizes(ImageFormat.JPEG); //保存照片尺寸
                previewSize = configurationMap.getOutputSizes(SurfaceTexture.class); //预览尺寸
            }
            boolean exchange = exchangeWidthAndHeight(mDisplayRotation, mCameraSensorOrientation);

            mSavePicSize = getBestSize(exchange ? mSavePicSize.getHeight() : mSavePicSize.getWidth(),
                    exchange ? mSavePicSize.getWidth() : mSavePicSize.getHeight(),
                    exchange ? mSavePicSize.getHeight() : mSavePicSize.getWidth(),
                    exchange ? mSavePicSize.getWidth() : mSavePicSize.getHeight(), Arrays.asList(savePicSize));

            mPreviewSize = getBestSize(exchange ? mPreviewSize.getHeight() : mPreviewSize.getWidth(),
                    exchange ? mPreviewSize.getWidth() : mPreviewSize.getHeight(),
                    exchange ? mSurfaceView.getHeight() : mSurfaceView.getWidth(),
                    exchange ? mSurfaceView.getWidth() : mSurfaceView.getHeight(), Arrays.asList(previewSize));
            //此处预览的大小保持不变
            mSurfaceView.getHolder().setFixedSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());

            Log.d(TAG, "预览最优尺寸 ：" + mPreviewSize.getWidth() + " * " + mPreviewSize.getHeight() + ", 比例  " + mPreviewSize.getWidth() / (mPreviewSize.getHeight() * 1.0));
            Log.d(TAG, "保存图片最优尺寸 ：" + mSavePicSize.getWidth() + "* " + mSavePicSize.getHeight() + ", 比例  " + mSavePicSize.getWidth() / (mSavePicSize.getHeight() * 1.0));


            mImageReader = ImageReader.newInstance(mSavePicSize.getWidth(), mSavePicSize.getHeight(), ImageFormat.JPEG, 1);
            mImageReader.setOnImageAvailableListener(onImageAvailableListener, mCameraHandler);

            openCamera();
        } catch (CameraAccessException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * 打开相机
     */
    private void openCamera() {
        if (ContextCompat.checkSelfPermission(mActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(mActivity, PERMISSIONS_STORAGE, 11);
            Toast.makeText(mActivity, "没有相机权限！", Toast.LENGTH_SHORT).show();
        } else {
            try {
                mCameraManager.openCamera(mCameraId, new CameraDevice.StateCallback() {
                    @Override
                    public void onOpened(@NonNull CameraDevice cameraDevice) {
                        Log.d(TAG, "onOpened");
                        mCameraDevice = cameraDevice;
                        createCaptureSession(cameraDevice);
                        cameraState = true;
                    }

                    @Override
                    public void onDisconnected(@NonNull CameraDevice cameraDevice) {
                        Log.d(TAG, "onDisconnected");
                        cameraState = false;
                    }

                    @Override
                    public void onError(@NonNull CameraDevice cameraDevice, int i) {
                        Log.d(TAG, "camera opened onError!!");
                        cameraState = false;
                    }
                }, mCameraHandler);
            } catch (CameraAccessException exception) {
                exception.printStackTrace();
            }
        }
    }

    /**
     * 创建预览会话
     */
    private void createCaptureSession(CameraDevice cameraDevice) {
        try {
            Log.d(TAG, "createCaptureSession的CameraDevice ：" + cameraDevice);
            final CaptureRequest.Builder builder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
//            @SuppressLint("Recycle")
            Surface surface = mSurfaceView.getHolder().getSurface();
            builder.addTarget(surface);
            builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            builder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);

            List<Surface> list = new ArrayList<>();
            list.add(surface);
//            list.add(mImageReader.getSurface());
            cameraDevice.createCaptureSession(list, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    mCameraCaptureSession = session;
                    try {

                        session.setRepeatingRequest(builder.build(), null, mCameraHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    Log.d(TAG, "onConfigureFailed: 开启预览会话失败");
                }
            }, mCameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private final CameraCaptureSession.CaptureCallback mCaptureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            canExchangeCamera = true;
        }

        @SuppressLint("ShowToast")
        @Override
        public void onCaptureFailed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureFailure failure) {
            super.onCaptureFailed(session, request, failure);
            Log.d(TAG, "onCaptureFailed");
        }
    };

    private final ImageReader.OnImageAvailableListener onImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader imageReader) {
            Image image = imageReader.acquireNextImage();
            ByteBuffer byteBuffer = image.getPlanes()[0].getBuffer();
//            val byteArray = ByteArray(byteBuffer.remaining())
            byte[] data = new byte[byteBuffer.remaining()];
            byteBuffer.get(data);
            image.close();
            savePic(data, mCameraSensorOrientation == 270);
        }
    };


    /**
     * 根据提供的参数值返回与指定宽高相等或最接近的尺寸
     *
     * @param targetWidth  目标宽度
     * @param targetHeight 目标高度
     * @param maxWidth     最大宽度(即TextureView的宽度)
     * @param maxHeight    最大高度(即TextureView的高度)
     * @param sizeList     支持的Size列表
     * @return 返回与指定宽高相等或最接近的尺寸
     */
    private Size getBestSize(int targetWidth, int targetHeight, int maxWidth, int maxHeight, List<Size> sizeList) {
        ArrayList<Size> bigEnough = new ArrayList<>();     //比指定宽高大的Size列表
        ArrayList<Size> notBigEnough = new ArrayList<>();  //比指定宽高小的Size列表

        for (Size size : sizeList) {

            //宽<=最大宽度  &&  高<=最大高度  &&  宽高比 == 目标值宽高比
            if (size.getWidth() <= maxWidth && size.getHeight() <= maxHeight
                    && size.getWidth() == size.getHeight() * targetWidth / targetHeight) {
                if (size.getWidth() >= targetWidth && size.getHeight() >= targetHeight)
                    bigEnough.add(size);
                else
                    notBigEnough.add(size);
            }
            Log.d(TAG, "系统支持的尺寸: " + size.getWidth() + " * " + size.getHeight() + " ,  比例 ：" + size.getWidth() / (size.getHeight() * 1.0));
        }

        Log.d(TAG, "最大尺寸 ：" + maxWidth + "* " + maxHeight + ", 比例 ：" + targetWidth / (targetHeight * 1.0));
        Log.d(TAG, "目标尺寸 ：" + targetWidth + " * " + targetHeight + ", 比例 ：" + targetWidth / (targetHeight * 1.0));

        //选择bigEnough中最小的值  或 notBigEnough中最大的值
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else if (notBigEnough.size() > 0) {
            return Collections.max(notBigEnough, new CompareSizesByArea());
        } else {
            return sizeList.get(0);
        }
    }

    /**
     * 根据提供的屏幕方向 [displayRotation] 和相机方向 [sensorOrientation] 返回是否需要交换宽高
     */
    private boolean exchangeWidthAndHeight(int displayRotation, int sensorOrientation) {
        boolean exchange = false;
        switch (displayRotation) {
            case Surface.ROTATION_0:
            case Surface.ROTATION_180:
                if (sensorOrientation == 90 || sensorOrientation == 270) {
                    exchange = true;
                }
                break;
            case Surface.ROTATION_90:
            case Surface.ROTATION_270:
                if (sensorOrientation == 0 || sensorOrientation == 180) {
                    exchange = true;
                }
                break;
            default:
                Log.d(TAG, "Display rotation is invalid: " + displayRotation);
                break;
        }

        Log.d(TAG, "屏幕方向  " + displayRotation);
        Log.d(TAG, "相机方向  " + sensorOrientation);
        return exchange;
    }

    public void releaseCamera() {
        if (mCameraCaptureSession != null) {
            mCameraCaptureSession.close();
            mCameraCaptureSession = null;
        }
        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
        if (mImageReader != null) {
            mImageReader.close();
            mImageReader = null;
        }
        canExchangeCamera = false;
    }

    public void releaseThread() {
        handlerThread.quitSafely();
        handlerThread = null;
    }

    private void savePic(byte[] data, Boolean isMirror) {
        try {
            long temp = System.currentTimeMillis();
            final File picFile = PicUtils.createCameraFile(fileName);
            if (picFile != null && data != null) {
                Bitmap rawBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                final Bitmap resultBitmap = isMirror ? PicUtils.mirror(rawBitmap) : rawBitmap;
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(mActivity).load(resultBitmap).override(240, 240).into(mImageView);
                    }
                });
                PicUtils.saveMyBitmap(resultBitmap, picFile.getAbsolutePath());
                Log.d(TAG, "图片已保存! 耗时：+" + (System.currentTimeMillis() - temp) + "路径：+" + picFile.getAbsolutePath());
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "savePic: fail" + e.getMessage());
        }
    }

    private static class CompareSizesByArea implements Comparator<Size> {
        @Override
        public int compare(Size size, Size t1) {
            return Long.signum((long) size.getWidth() * size.getHeight() - (long) t1.getWidth() * t1.getHeight());
        }
    }
}
