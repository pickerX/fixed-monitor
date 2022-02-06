package com.lib.record;

import android.content.Context;
import android.text.TextUtils;

import com.lib.camera.view.AutoFitSurfaceView;

/**
 * @author pickerx
 * @date 2022/1/27 8:51 下午
 */
public class ConfigBuilder {
    private final Context context;
    /**
     * 录制时长
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
    boolean preview;
    boolean loop;

    String cameraOrientation;
    AutoFitSurfaceView target;

    public ConfigBuilder(Context context) {
        this.context = context;
    }

    /**
     * 设置录制时长:分钟
     */
    public ConfigBuilder setDuration(int duration) {
        this.duration = duration;
        return this;
    }

    public ConfigBuilder setDirectory(String directory) {
        this.directory = directory;
        return this;
    }

    public ConfigBuilder setPreview(boolean preview) {
        this.preview = preview;
        return this;
    }

    /**
     * 设置 最多预留的设备空间
     */
    public ConfigBuilder setMaxReserveSize(int maxReserveSize) {
        this.maxReserveSize = maxReserveSize;
        return this;
    }

    public ConfigBuilder setCameraOrientation(String cameraOrientation) {
        this.cameraOrientation = cameraOrientation;
        return this;
    }

    public ConfigBuilder setTarget(AutoFitSurfaceView target) {
        this.target = target;
        if (target != null) this.preview = true;
        return this;
    }

    public ConfigBuilder setLoop(boolean loop) {
        this.loop = loop;
        return this;
    }

    public Config build() {
        Config c = new Config();

        if (duration <= 0) duration = Monitor.DEFAULT_DURATION;
        if (maxReserveSize <= 0) maxReserveSize = Monitor.DEFAULT_MAX_RESERVE_SIZE;
        // default back camera
        if (TextUtils.isEmpty(cameraOrientation)) cameraOrientation = Monitor.FACING_BACK;

        c.target = target;
        c.preview = preview;
        c.duration = duration;
        c.directory = directory;
        c.maxReserveSize = maxReserveSize;
        c.cameraOrientation = cameraOrientation;
        c.loop = loop;
        return c;
    }

}
