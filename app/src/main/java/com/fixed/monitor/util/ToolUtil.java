package com.fixed.monitor.util;

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
}
