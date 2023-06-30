package com.example.camerel;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class PicUtils {


    public static final String rootFolderPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath();
    private static final String TAG = PicUtils.class.getSimpleName();

    /**
     * 获取图片地址列表
     *
     * @param file 目标文件夹
     * @return 目标文件地址集合
     */
    public static ArrayList<String> imagePath(ArrayList<String> list,File file) {
        if(list == null){
            list = new ArrayList<>();
        }else {
            list.clear();
        }
        if (!file.exists()) {
            Log.d(TAG, "file is not exists");
            return list;
        }
        File[] files = file.listFiles();
        for (File f : files) {
            if (f.getName().endsWith(".jpg")) {
                list.add(f.getAbsolutePath());
            }
        }
        Collections.sort(list, Collections.<String>reverseOrder());
        return list;
    }

    /**
     * 创建相机文件
     *
     * @param folderName 目标文件名字（非全路径）
     * @return 目标文件
     */
    public static File createCameraFile(String folderName) {
        try {
            File rootFile = new File(rootFolderPath + File.separator + folderName);
            if (!rootFile.exists())
                rootFile.mkdirs();

            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String fileName = "IMG_" + timeStamp + ".jpg";
            File file = new File(rootFile.getAbsolutePath() + File.separator + fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            return file;
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        return null;
    }

    /**
     * 保存bitmap到文件中
     *
     * @param mBitmap  目标bitmap
     * @param fileName 目标文件
     */
    public static void saveMyBitmap(Bitmap mBitmap, String fileName) {
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(fileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
        try {
            fOut.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * bitmap 镜像方法
     *
     * @param rawBitmap 目标bitmap
     * @return 目的bitmap
     */
    public static Bitmap mirror(Bitmap rawBitmap) {
        Matrix matrix = new Matrix();
        matrix.postScale(-1f, 1f);
        return Bitmap.createBitmap(rawBitmap, 0, 0, rawBitmap.getWidth(), rawBitmap.getHeight(), matrix, true);
    }

    /**
     * 读取sdcard文件夹中的图片，并生成略缩图
     *
     * @return
     * @throws FileNotFoundException
     */
    private Map<String, Bitmap> buildThum(ArrayList<String> imgList, String foldName) throws FileNotFoundException {
        File baseFile = new File(foldName);
        // 使用TreeMap，排序问题就不需要纠结了
        Map<String, Bitmap> maps = new TreeMap<String, Bitmap>();
        if (baseFile != null && baseFile.exists()) {
            imgList = imagePath(imgList,baseFile);

            if (!imgList.isEmpty()) {
                for (int i = 0; i < imgList.size(); i++) {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true; // 设置了此属性一定要记得将值设置为false
                    Bitmap bitmap = BitmapFactory.decodeFile(imgList.get(i), options);
                    options.inJustDecodeBounds = false;
                    int be = options.outHeight / 40;
                    if (be <= 0) {
                        be = 10;
                    }
                    options.inSampleSize = be;
                    bitmap = BitmapFactory.decodeFile(imgList.get(i), options);
                    maps.put(imgList.get(i), bitmap);
                }
            }
        }

        return maps;
    }
}
