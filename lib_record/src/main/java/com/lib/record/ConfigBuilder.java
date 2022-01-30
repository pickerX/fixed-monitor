package com.lib.record;

/**
 * @author pickerx
 * @date 2022/1/27 8:51 下午
 */
public class ConfigBuilder {
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

    public ConfigBuilder() {

    }

    public Config build() {
        Config c = new Config();
        if (duration <= 0) {
            duration = Monitor.DEFAULT_DURATION;
        }
        if (directory == null || directory.isEmpty()) {
            directory = Monitor.DEFAULT_SAVE_DIR;
        }
        if (maxReserveSize <= 0) {
            maxReserveSize = Monitor.DEFAULT_MAX_RESERVE_SIZE;
        }
        c.duration = duration;
        c.directory = directory;
        c.maxReserveSize = maxReserveSize;

        return c;
    }

}
