package com.example.camerel;

import android.content.Context;
import android.content.Intent;

import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.viewpager.widget.ViewPager;

import java.io.File;
import java.util.ArrayList;


public class CameraPictureView extends RelativeLayout {
    private Context mContext;
    private static final String TAG = "CameraPictureView";

    private TextView currentNum;
    private ArrayList<String> imgList;
    private ViewPager viewPager;
    private TextView totalNum;
    private PictureClickListener pictureClickListener;

    private MyPagerAdapter myPagerAdapter;

    public void setPictureClickListener(PictureClickListener pictureClickListener) {
        this.pictureClickListener = pictureClickListener;
    }

    public CameraPictureView(Context context) {
        this(context, null);
    }

    public CameraPictureView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraPictureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        View view = LayoutInflater.from(context).inflate(R.layout.activity_camera_picture, this);
        initView(view);
    }

    private void initView(View view) {
        viewPager = view.findViewById(R.id.viewPager);
        currentNum = view.findViewById(R.id.currentNum);
        totalNum = view.findViewById(R.id.totalNum);
        imgList = PicUtils.imagePath(imgList, new File(PicUtils.rootFolderPath + File.separator + CameraManager.fileName));
        totalNum.setText(String.valueOf(imgList.size()));
        myPagerAdapter = new MyPagerAdapter(mContext, imgList);
        //设置缓存页数
        viewPager.setAdapter(myPagerAdapter);
        //添加页面更改监听器
        viewPager.addOnPageChangeListener(onPageChangeListener);
        viewPager.setCurrentItem(0);
        view.findViewById(R.id.iv_back).setOnClickListener(v -> pictureClickListener.onClick());
    }

    public void notifyData() {
        imgList = PicUtils.imagePath(imgList, new File(PicUtils.rootFolderPath + File.separator + CameraManager.fileName));
        totalNum.setText(String.valueOf(imgList.size()));
        myPagerAdapter.notifyDataSetChanged();
        viewPager.setCurrentItem(0);
    }

    private ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener() {
        /**
         * function: 当前页面滚动的时候回调这个方法
         * @param position 当前页面的位置
         * @param positionOffset 滑动页面的百分比
         * @param positionOffsetPixels 滑动的像素数
         * @return
         */
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        /**
         * function: 当页面被选中时，会回调这个方法
         * @param position 被选中的页面的位置
         * @return
         */
        @Override
        public void onPageSelected(int position) {
            Log.e(TAG, "onPageSelected: MainActivity" + position);
            //当新页面选中时调用此方法，position 为新选中页面的位置索引
            //在所选页面的时候,点点图片也要发生变化
            currentNum.setText(String.valueOf(position + 1));
            Log.e(TAG, "onPageSelected: " + imgList.size());
        }

        /**
         * function: 当页面滚动状态变化时，会回调这个方法
         * 有三种状态：静止、滑动、拖拽（这里区别滑动和拖拽，以手指是否接触页面为准）
         * @param state 当前状态
         * @return
         */
        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    /**
     * 通知外部 当前界面消失
     */
    public interface PictureClickListener {
        void onClick();
    }

}
