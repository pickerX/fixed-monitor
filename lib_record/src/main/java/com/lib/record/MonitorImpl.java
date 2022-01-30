package com.lib.record;

/**
 * @author pickerx
 * @date 2022/1/27 8:54 下午
 */
public class MonitorImpl implements Monitor {
    XCamera mCamera;

    public MonitorImpl(Config config) {
        this.mCamera = new XCamera(config);
    }

    @Override
    public boolean recordNow() {


        return false;
    }

    @Override
    public void release() {

    }

}
