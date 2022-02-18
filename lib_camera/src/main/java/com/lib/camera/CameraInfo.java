package com.lib.camera;

import android.util.Size;

/**
 * @author pickerx
 * @date 2022/2/6 4:02 下午
 */
public class CameraInfo {
    String name;
    String cameraId;
    Size size;
    int fps;

    public CameraInfo(String name, String cameraId, Size size, int fps) {
        this.name = name;
        this.cameraId = cameraId;
        this.size = size;
        this.fps = fps;
    }

    @Override
    public String toString() {
        return "CameraInfo{" +
                "name='" + name + '\'' +
                '}';
    }
}
