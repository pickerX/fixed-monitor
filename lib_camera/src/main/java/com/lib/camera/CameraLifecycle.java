package com.lib.camera;

/**
 * @author pickerx
 * @date 2022/2/6 3:38 下午
 */
public interface CameraLifecycle {

    /**
     * camera preview prepared
     */
    void onPrepared();

    void onStarted(long startMillis);

    void onStopped(long stopMillis, String name, String path, long size,long duringTime,String coverPath);

}
