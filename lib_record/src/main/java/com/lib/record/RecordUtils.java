package com.lib.record;

import android.content.Context;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author pickerx
 * @date 2022/1/28 4:34 下午
 */
public class RecordUtils {

    public static File createFile(Context context, String extension) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS", Locale.US);
        String date = sdf.format(new Date());
        return new File(context.getFilesDir(),
                String.format("VID_%s.%s", date, extension));
    }

}
