package com.example.camerel;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

public class CameraView extends RelativeLayout implements CameraPreView.PreviewClickLister {
    private Context mContext;
    private ImageButton iv_take;
    private CameraPreView cameraPreView;

    public CameraView(Context context) {
        this(context, null);
    }

    public CameraView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        View view = LayoutInflater.from(context).inflate(R.layout.activity_camera, this);
        iv_take = view.findViewById(R.id.iv_take);
        cameraPreView = new CameraPreView(mContext);
        cameraPreView.setPreviewClickLister(this);
        cameraPreView.setVisibility(INVISIBLE);
        ((RelativeLayout) iv_take.getParent()).addView(cameraPreView);
        initLister();

    }

    private void initLister() {
        //点击打开拍照事件  创建预览界面及展示
        iv_take.setOnClickListener(v -> {
            iv_take.setVisibility(GONE);
                cameraPreView.setVisibility(VISIBLE);
                cameraPreView.initCamera();

        });
    }

    /**
     * 回调预览界面消失事件
     */
    @Override
    public void onclick() {
        iv_take.setVisibility(VISIBLE);
        cameraPreView.setVisibility(GONE);
    }
}
