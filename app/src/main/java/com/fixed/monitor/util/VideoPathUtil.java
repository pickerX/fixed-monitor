package com.fixed.monitor.util;

import android.content.Context;

import com.fixed.monitor.base.ACache;
import com.fixed.monitor.model.App;

public class VideoPathUtil {

    public static String VIDEO_SAVECACHE_RECORDTIME_KEY = "VIDEO_SAVECACHE_RECORDTIME_KEY";

    public static String VIDEO_SAVECACHE_PATH_KEY = "VIDEO_SAVECACHE_PATH_KEY";

    /**
     * @param
     * @return
     * @description 保存的录制时间
     * @author jiejack
     * @time 2022/2/3 2:58 下午
     */
    public static void saveVideoRecordTime(Context context, int time) {
        ACache.get(App.getApp()).put(VIDEO_SAVECACHE_RECORDTIME_KEY, time + "");
    }


    /**
     * @param
     * @return
     * @description 返回保存的录制时间
     * @author jiejack
     * @time 2022/2/3 3:03 下午
     */
    public static int getVideoRecordTime(Context context) {
        return ToolUtil.string2Int(ACache.get(App.getApp()).getAsString(VIDEO_SAVECACHE_RECORDTIME_KEY));
    }

    /**
     * @param
     * @return
     * @description 设置录像保存路径
     * @author jiejack
     * @time 2022/2/3 2:58 下午
     */
    public static void savePath(Context context, String path) {
        ACache.get(App.getApp()).put(VIDEO_SAVECACHE_PATH_KEY, path);
    }


    /**
     * @param
     * @return
     * @description 返回当前录像保存路径
     * @author jiejack
     * @time 2022/2/3 3:03 下午
     */
    public static String getPath(Context context) {
        return ACache.get(App.getApp()).getAsString(VIDEO_SAVECACHE_PATH_KEY);
    }
}
