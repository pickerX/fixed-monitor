package com.lib.record;

import androidx.camera.view.PreviewView;

import com.lib.camera.view.AutoFitSurfaceView;

/**
 *
 */
public class Config {
    /**
     * 录制时长:分钟
     */
    public int duration;
    /**
     * 指定文件存储位置
     */
    public String directory;
    /**
     * 最多预留的设备空间
     */
    public int maxReserveSize;
    /**
     * 录制的摄像头
     */
    public String cameraId;
    /**
     * 摄像头类型：前置，后置，外部
     */
    public String cameraOrientation;
    /**
     * 是否需要预览
     */
    public boolean preview;
    /**
     * 是否循环录制
     */
    public boolean loop;
    public AutoFitSurfaceView target;
    public PreviewView previewTarget;
    /**
     * use cameraX API
     */
    public boolean cameraX;
}
