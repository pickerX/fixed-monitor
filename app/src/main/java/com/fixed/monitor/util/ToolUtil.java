package com.fixed.monitor.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.fixed.monitor.model.App;

public class ToolUtil {

    public static int string2Int(String s) {
        try {
            return Integer.valueOf(s);
        } catch (Exception e) {
            return 0;
        }
    }

    public static double string2Double(String s) {
        try {
            return Double.valueOf(s);
        } catch (Exception e) {
            return 0;
        }
    }

    public static long string2Long(String s) {
        try {
            return Long.valueOf(s);
        } catch (Exception e) {
            return 0;
        }
    }


    public static float string2Float(String s) {
        try {
            return Float.valueOf(s);
        } catch (Exception e) {
            return 0;
        }
    }

    /*****
     * String 是否空
     * ******/
    public final static boolean isNull(String value) {

        if (value == null || value.equals("")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * @param
     * @return
     * @description 重启APP
     * @author jieja
     * @time 2022/2/7 15:10
     */
    public static void reStartApp(Context context) {
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        intent.putExtra("REBOOT", "reboot");
        PendingIntent restartIntent = PendingIntent.getActivity(App.getApp(), 0, intent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, restartIntent);
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}
