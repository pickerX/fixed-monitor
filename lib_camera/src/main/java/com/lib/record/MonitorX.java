package com.lib.record;

import android.content.Context;

import com.lib.camera.CameraLifecycle;
import com.lib.camera.CameraX;

/**
 * @author pengfei.huang
 * create on 2022/3/9
 */
public class MonitorX implements Monitor {

    private final Config config;
    private CameraX mCameraX;

    public MonitorX(Config config) {
        this.config = config;
        mCameraX = new CameraX(config);
    }

    @Override
    public void recordNow(Context context) {
        mCameraX.prepare(context);
    }

    @Override
    public void stop() {

    }

    @Override
    public void release() {

    }

    @Override
    public void setStateCallback(CameraLifecycle callback) {

    }
}
