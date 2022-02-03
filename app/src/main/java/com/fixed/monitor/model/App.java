package com.fixed.monitor.model;

import android.app.Application;

import com.fixed.monitor.base.CrashExpection;

import java.io.File;

import xyz.doikki.videoplayer.player.AndroidMediaPlayerFactory;
import xyz.doikki.videoplayer.player.VideoViewConfig;
import xyz.doikki.videoplayer.player.VideoViewManager;

public class App extends Application {

    private static App app;
    public static App getApp() {
        return app;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        init();
    }

    /**
     * @param
     * @return
     * @description 初始化内容
     * @author jiejack
     * @time 2022/2/2 8:50 下午
     */
    private void init() {
        Thread.setDefaultUncaughtExceptionHandler(
                CrashExpection.getInstance(this));
        initVideoPlayer();
    }

    /**
     * @param
     * @return
     * @description 初始化dkplayer组件
     * @author jiejack
     * @time 2022/2/2 8:49 下午
     */
    private void initVideoPlayer() {
        VideoViewManager.setConfig(VideoViewConfig.newBuilder()
                //使用使用IjkPlayer解码
//                .setPlayerFactory(IjkPlayerFactory.create())
                //使用ExoPlayer解码
//                .setPlayerFactory(ExoMediaPlayerFactory.create())
                //使用MediaPlayer解码
                .setPlayerFactory(AndroidMediaPlayerFactory.create())
                .build());
    }


}
