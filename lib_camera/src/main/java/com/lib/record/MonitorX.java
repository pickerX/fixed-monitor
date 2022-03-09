package com.lib.record;

import android.content.Context;
import android.util.Log;

import com.lib.camera.CameraLifecycle;
import com.lib.camera.CameraX;

/**
 * @author pengfei.huang
 * create on 2022/3/9
 */
public class MonitorX implements Monitor {

    private final Config config;
    private final CameraX mCameraX;
    private CameraLifecycle callback;

    public MonitorX(Config config) {
        this.config = config;
        mCameraX = new CameraX(config);
        initCameraX();
    }

    private void initCameraX() {
        mCameraX.setCameraLifecycle(new CameraLifecycle() {
            @Override
            public void onPrepared() {
                if (callback != null) callback.onPrepared();
                mCameraX.startRecord();
            }

            @Override
            public void onStarted(long startMillis) {
                if (callback != null) callback.onStarted(startMillis);
            }

            @Override
            public void onStopped(long stopMillis, String name, String path, long size, long duringTime, String coverPath) {
                if (callback != null)
                    callback.onStopped(stopMillis, name, path, size, duringTime, coverPath);

                if (config.loop) {
                    dispatchNext();
                }
            }

            @Override
            public void lifeLog(int type, String msg, long time) {
                if (callback != null) callback.lifeLog(type, msg, time);
            }

            @Override
            public void lifeErro(String msg, long time, Exception e) {
                if (callback != null) callback.lifeErro(msg, time, e);
            }
        });
    }

    private void dispatchNext() {
        mCameraX.startRecord();
    }

    @Override
    public void recordNow(Context context) {
        mCameraX.prepare(context);
    }

    @Override
    public void stop() {
        mCameraX.stopRecord();
    }

    @Override
    public void release() {
        mCameraX.release();
    }

    @Override
    public void setStateCallback(CameraLifecycle callback) {
        this.callback = callback;
    }
}
