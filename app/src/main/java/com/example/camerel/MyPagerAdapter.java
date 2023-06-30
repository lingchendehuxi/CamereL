package com.example.camerel;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;


public class MyPagerAdapter extends PagerAdapter {
    private final Context context;
    private final List<String> imgList;

    public MyPagerAdapter(Context context, ArrayList<String> imgList) {
        this.context = context;
        this.imgList = imgList;
    }

    @Override
    public int getCount() {
        //返回int的最大值 可以一直滑动
        return imgList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        position %= imgList.size();
        ImageView imageView = new ImageView(context);
        //设置图片缩放类型
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        //把当前的下标通过setTag方法设置进去
        imageView.setTag(position);
        Glide.with(context).load(imgList.get(position)).override(240,240).into(imageView);
        container.addView(imageView);
        return imageView;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

}
