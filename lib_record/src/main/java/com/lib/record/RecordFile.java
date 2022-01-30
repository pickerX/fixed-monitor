package com.lib.record;

/**
 * 录制文件记录
 *
 * @author pickerx
 * @date 2022/1/28 10:19 上午
 */
public class RecordFile {
    long startTime;
    long endTime;
    String fileName;

    /**
     * 是否到底配置的时长，切割文件
     */
    boolean isFullTime(long duration) {
        return (endTime - startTime) / 1000 >= duration;
    }
}
