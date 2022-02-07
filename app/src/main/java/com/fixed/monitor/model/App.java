package com.fixed.monitor.model;

import android.app.Application;
import android.content.Context;

import com.fixed.monitor.base.CrashExpection;
import com.fixed.monitor.base.DatabaseHelper;
import com.fixed.monitor.util.VideoPathUtil;
import com.scwang.smart.refresh.footer.ClassicsFooter;
import com.scwang.smart.refresh.header.ClassicsHeader;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshFooter;
import com.scwang.smart.refresh.layout.api.RefreshHeader;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.DefaultRefreshFooterCreator;
import com.scwang.smart.refresh.layout.listener.DefaultRefreshHeaderCreator;

import java.io.File;

import xyz.doikki.videoplayer.player.AndroidMediaPlayerFactory;
import xyz.doikki.videoplayer.player.VideoViewConfig;
import xyz.doikki.videoplayer.player.VideoViewManager;

public class App extends Application {

    private static App app;

    public static App getApp() {
        return app;
    }


    //数据库helper
    private DatabaseHelper dbinstance;

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
        initSmartRefreshLayout();
        initVideoRecordParameter();
    }

    /**
     * @description 初始化录制参数
     * @param 
     * @return 
     * @author jieja
     * @time 2022/2/7 14:53
     */
    private void initVideoRecordParameter() {
        if (VideoPathUtil.getVideoRecordTime(this) == 0) {
            VideoPathUtil.saveVideoRecordTime(this, 60);//默认60分钟保存一次录像
        }
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

    private void initSmartRefreshLayout() {
        //设置全局的Header构建器
        SmartRefreshLayout.setDefaultRefreshHeaderCreator(new DefaultRefreshHeaderCreator() {
            @Override
            public RefreshHeader createRefreshHeader(Context context, RefreshLayout layout) {
//                layout.setPrimaryColorsId(R.color.c_ffffff, R.color.c_000000);//全局设置主题颜色
                return new ClassicsHeader(context);//.setTimeFormat(new DynamicTimeFormat("更新于 %s"));//指定为经典Header，默认是 贝塞尔雷达Header
            }
        });
        //设置全局的Footer构建器
        SmartRefreshLayout.setDefaultRefreshFooterCreator(new DefaultRefreshFooterCreator() {
            @Override
            public RefreshFooter createRefreshFooter(Context context, RefreshLayout layout) {
                //指定为经典Footer，默认是 BallPulseFooter
                return new ClassicsFooter(context);
            }
        });
    }

    /**
     * @return
     */
    public synchronized DatabaseHelper getHelper() {
        if (dbinstance == null) {
            synchronized (DatabaseHelper.class) {
                if (dbinstance == null)
                    dbinstance = new DatabaseHelper(this);
            }
        }
        return dbinstance;
    }

}
