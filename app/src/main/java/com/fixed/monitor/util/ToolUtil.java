package com.fixed.monitor.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.fixed.monitor.model.App;

import java.text.SimpleDateFormat;
import java.util.Date;

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
     * 将秒数转换为日时分秒，
     * @param second
     * @return
     */
    public static String secondToTime(long second){
        long days = second / 86400;            //转换天数
        second = second % 86400;            //剩余秒数
        long hours = second / 3600;            //转换小时
        second = second % 3600;                //剩余秒数
        long minutes = second /60;            //转换分钟
        second = second % 60;                //剩余秒数
        if(days>0){
            return days + "天" + hours + "小时" + minutes + "分" + second + "秒";
        }else{
            return hours + "小时" + minutes + "分" + second + "秒";
        }
    }


    public static String timestamp2String(long timestamp, String format) {
        String sd = "";
        format = (isNull(format) ? "yyyy-MM-dd HH:mm:ss" : format);
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            sd = sdf.format(new Date(timestamp));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sd;
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
