package com.fixed.monitor.util;

import android.content.Context;

import com.fixed.monitor.base.ACache;
import com.fixed.monitor.model.App;

public class PasswordUtil {

    public static String PASSWORD_PATH_KEY = "PASSWORD_PATH_KEY";
    /**
     * @param
     * @return
     * @description 设置录像保存路径
     * @author jiejack
     * @time 2022/2/3 2:58 下午
     */
    public static void savePsw(Context context, String path){
        ACache.get(App.getApp()).put(PASSWORD_PATH_KEY,path);
    }


    /**
     * @param
     * @return
     * @description 返回当前录像保存路径
     * @author jiejack
     * @time 2022/2/3 3:03 下午
     */
    public static String getPsw(Context context){
        String psw =  ACache.get(App.getApp()).getAsString(PASSWORD_PATH_KEY);
        return ToolUtil.isNull(psw)?"000000":psw;
    }
}
