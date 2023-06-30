package com.example.camerel;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;


public class CameraPreView extends ConstraintLayout implements CameraPictureView.PictureClickListener {

    private String TAG = "CameraPreView";
    private Context mContext;
    private CameraManager mCameraManager;
    private SurfaceView textureView;

    private CameraPictureView cameraPictureView;
    private ImageView btnTakePic;
    private ImageView ivExchange;
    private ImageView ivTemp;
    private ImageView ivBack;
    private PreviewClickLister previewClickLister;

    public void setPreviewClickLister(PreviewClickLister previewClickLister) {
        this.previewClickLister = previewClickLister;
    }

    public CameraPreView(Context context) {
        this(context, null);
    }

    public CameraPreView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraPreView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        View view = LayoutInflater.from(context).inflate(R.layout.activity_camera_preview, this);
        initView(view);
    }

    private void initView(View view) {
        textureView = view.findViewById(R.id.textureView);
        btnTakePic = view.findViewById(R.id.btnTakePic);
        ivExchange = view.findViewById(R.id.ivExchange);
        ivTemp = view.findViewById(R.id.ivTemp);
        ivBack = view.findViewById(R.id.iv_back);
        Log.d(TAG, "initView: ");
        mCameraManager = new CameraManager((Activity) mContext, textureView, ivTemp);
        cameraPictureView = new CameraPictureView(mContext);
        cameraPictureView.setPictureClickListener(this);
//        ((RelativeLayout) this.getParent()).addView(cameraPictureView);
        ArrayList<String> imgList = PicUtils.imagePath(null, new File(PicUtils.rootFolderPath + File.separator + CameraManager.fileName));
        RequestOptions options = new RequestOptions()
                .circleCropTransform();

        if (imgList.size() > 0) {
            Glide.with(mContext).asBitmap()
                    .load(imgList.get(0)).override(240, 240)
                    .into(ivTemp);
        } else {
            Glide.with(mContext).load(R.drawable.iv_photo).override(240, 240).into(ivTemp);
        }
        initLister();
    }

    public void initCamera() {
        mCameraManager.init();
    }

    private void initLister() {
        btnTakePic.setOnClickListener(v -> mCameraManager.takePicture());
        ivExchange.setOnClickListener(v -> mCameraManager.exchangeCamera());
        //创建预览界面
        ivTemp.setOnClickListener(v -> {
            this.setVisibility(GONE);
            cameraPictureView.notifyData();
            cameraPictureView.setVisibility(VISIBLE);
            //处理当前相机的资源操作
            mCameraManager.releaseCamera();
            mCameraManager.releaseThread();

        });
        //预览界面退出
        ivBack.setOnClickListener(v -> {
            previewClickLister.onclick();
            //处理当前相机的资源操作
            mCameraManager.releaseCamera();
            mCameraManager.releaseThread();
        });
    }

    /**
     * 回调图库消失事件
     * 再次初始化相机资源
     */
    @Override
    public void onClick() {
        cameraPictureView.setVisibility(GONE);
        this.setVisibility(VISIBLE);
        initCamera();
    }

    /**
     * 通知外部 当前界面消失
     */
    public interface PreviewClickLister {
        void onclick();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Log.d(TAG, "onAttachedToWindow: ");
    }
}
