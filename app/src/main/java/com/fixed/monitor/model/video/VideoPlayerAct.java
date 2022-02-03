package com.fixed.monitor.model.video;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;


import com.fixed.monitor.R;
import com.fixed.monitor.base.BaseAct;
import com.fixed.monitor.view.VideoTitileView;

import xyz.doikki.videocontroller.StandardVideoController;
import xyz.doikki.videocontroller.component.CompleteView;
import xyz.doikki.videocontroller.component.ErrorView;
import xyz.doikki.videocontroller.component.GestureView;
import xyz.doikki.videocontroller.component.PrepareView;
import xyz.doikki.videocontroller.component.VodControlView;
import xyz.doikki.videoplayer.player.VideoView;

public class VideoPlayerAct extends BaseAct {

    /**
     * @param
     * @return
     * @description
     * @author jiejack
     * @time 2022/2/2 4:57 下午
     */
    public static void openAct(Context context, String title ,String url) {
        Intent intent = new Intent(context, VideoPlayerAct.class);
        intent.putExtra("title", title);
        intent.putExtra("url", url);
        context.startActivity(intent);
    }

    VideoView videoView;

    @Override
    public int setLayoutID() {
        return R.layout.act_videoplayer;
    }

    @Override
    public void initView() {
        videoView    = findViewById(R.id.player);

    }

    @Override
    public void doBusiness() {
        String title = getIntent().getStringExtra("title");
        String url = getIntent().getStringExtra("url");
        if (TextUtils.isEmpty(url)) {
            finish();
            return;
        }
        videoView.setUrl(url); //设置视频地址
        StandardVideoController controller = new StandardVideoController(this);
//        controller.addDefaultControlComponent("回放", false);

        CompleteView completeView = new CompleteView(this);
        ErrorView errorView = new ErrorView(this);
        PrepareView prepareView = new PrepareView(this);
        prepareView.setClickStart();
        VideoTitileView videoTitileView = new VideoTitileView(this);
        videoTitileView.setTitle(title);
        controller.addControlComponent(completeView, errorView, prepareView, videoTitileView);
        controller.addControlComponent(new VodControlView(this));
        controller.addControlComponent(new GestureView(this));
        controller.setCanChangePosition(true);

        videoView.setVideoController(controller); //设置控制器
        videoView.startFullScreen();
        videoView.start(); //开始播放，不调用则不自动播放
    }

    @Override
    public void doWeakUp() {

    }


    @Override
    protected void onPause() {
        super.onPause();
        videoView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (videoView != null)
            videoView.resume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (videoView != null)
            videoView.release();
    }


    @Override
    public void onBackPressed() {
        if (videoView != null && !videoView.onBackPressed()) {
            super.onBackPressed();
        }
    }
}
