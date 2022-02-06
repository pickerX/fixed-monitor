package com.fixed.monitor.util;

import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.content.ContextCompat;


/**
 * Created by lzj on 2017/9/9.
 */

public class PermissionUtil {


    // 判断权限集合
    public static boolean lacksPermissions(Context mContext, String... permissions) {
        for (String permission : permissions) {
            if (lacksPermission(mContext, permission)) {
                return true;
            }
        }
        return false;
    }

    // 判断是否缺少权限
    private static boolean lacksPermission(Context mContext, String permission) {
        return ContextCompat.checkSelfPermission(mContext, permission) ==
                PackageManager.PERMISSION_DENIED;
    }


}
