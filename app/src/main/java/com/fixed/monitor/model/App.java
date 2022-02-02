package com.fixed.monitor.model;

import android.app.Application;

import xyz.doikki.videoplayer.player.AndroidMediaPlayerFactory;
import xyz.doikki.videoplayer.player.VideoViewConfig;
import xyz.doikki.videoplayer.player.VideoViewManager;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
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
