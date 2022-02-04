package com.lib.record;

import android.view.SurfaceView;

import com.lib.record.view.AutoFitSurfaceView;

/**
 *
 */
public class Config {
    /**
     * 录制时长:分钟
     */
    int duration;
    /**
     * 指定文件存储位置
     */
    String directory;
    /**
     * 最多预留的设备空间
     */
    int maxReserveSize;
    /**
     * 录制的摄像头
     */
    String cameraId;
    /**
     * 摄像头类型：前置，后置，外部
     */
    String cameraOrientation;
    /**
     * 是否需要预览
     */
    boolean preview;

    AutoFitSurfaceView target;
}
