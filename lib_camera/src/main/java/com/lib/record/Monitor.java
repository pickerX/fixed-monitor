package com.lib.record;

import android.content.Context;

import com.lib.camera.CameraLifecycle;

/**
 * 监听器 API
 *
 * @author pickerx
 * @date 2022/1/27 8:25 下午
 */
public interface Monitor {
    String FACING_BACK = "Back";
    String FACING_FRONT = "Front";
    String FACING_EXTERNAL = "External";
    String FACING_UNKNOWN = "Unknown";

    /**
     * 默认录制时长
     */
    int DEFAULT_DURATION = 60 * 60;
    /**
     * 默认设备最大的预留空间：1G
     */
    int DEFAULT_MAX_RESERVE_SIZE = 1024 * 1024;
    /**
     * 默认存储位置
     */
    String DEFAULT_SAVE_DIR = "/monitor";

    /**
     * 开始录制
     *
     * @param context
     * @return true 启动成功，false 失败
     */
    void recordNow(Context context);

    /**
     * stop monitoring
     */
    void stop();

    /**
     * 销毁资源，释放 context
     */
    void release();

    /**
     * 设置状态回调监听
     */
    void setStateCallback(CameraLifecycle callback);

    interface Factory {

        Monitor create(Config config);
    }

}
