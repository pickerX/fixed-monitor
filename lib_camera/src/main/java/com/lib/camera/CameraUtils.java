package com.lib.camera;

import android.content.Context;
import android.graphics.Point;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.util.Size;
import android.view.Display;
import android.view.Surface;

import com.lib.record.Monitor;
import com.lib.util.StorageUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

/**
 * @author pickerx
 * @date 2022/1/28 4:34 下午
 */
public class CameraUtils {
    public static final String extensions = "3gp";

    public static File getDefaultDir(Context context, String dir) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File root = Environment.getExternalStorageDirectory();
            return new File(root.getAbsolutePath() + File.separator + dir);
        }

        return context.getExternalFilesDir(dir);
    }

    public static File createFile(Context context, String dir, String extension) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS", Locale.US);
        String date = sdf.format(new Date());
        if (dir.equals(Monitor.DEFAULT_SAVE_DIR)) {
            File parent = getDefaultDir(context, dir);
            if (!parent.exists()) parent.mkdirs();

            return new File(parent, String.format("VOD_%s.%s", date, extension));
        } else {
            File parent = new File(dir);
            if (!parent.exists()) parent.mkdirs();

            // 指定目录时
            return new File(dir, String.format("VOD_%s.%s", date, extension));
        }
    }

    /**
     * Returns a [SmartSize] object for the given [Display]
     */
    private static SmartSize getDisplaySmartSize(Display display) {
        Point outPoint = new Point();
        display.getRealSize(outPoint);
        return new SmartSize(outPoint.x, outPoint.y);
    }

    /**
     * Returns the largest available PREVIEW size. For more information, see:
     * https://d.android.com/reference/android/hardware/camera2/CameraDevice and
     * https://developer.android.com/reference/android/hardware/camera2/params/StreamConfigurationMap
     *
     * @param format 根据输出格式获取尺寸
     */
    public static <T> Size getPreviewOutputSize(
            Display display,
            CameraCharacteristics characteristics,
            Class<T> targetClass,
            int format) {
        // Find which is smaller: screen or 1080p
        SmartSize screenSize = getDisplaySmartSize(display);
        boolean hdScreen = screenSize.max() >= SIZE_1080P.max()
                || screenSize.min() >= SIZE_1080P.min();

        SmartSize maxSize;
        if (hdScreen) maxSize = SIZE_1080P;
        else maxSize = screenSize;

        // If image format is provided, use it to determine supported sizes; else use target class
        StreamConfigurationMap config = characteristics.get(
                CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        if (format == 0)
            assert (StreamConfigurationMap.isOutputSupportedFor(targetClass));
        else
            assert (config.isOutputSupportedFor(format));

        Size[] allSizes;
        if (format == 0) allSizes = config.getOutputSizes(targetClass);
        else allSizes = config.getOutputSizes(format);

        // Get available sizes and sort them by area from largest to smallest
        Arrays.sort(allSizes, new Comparator<Size>() {
            @Override
            public int compare(Size s1, Size s2) {
                return s2.getWidth() * s2.getHeight() - s1.getWidth() * s1.getHeight();
            }
        });
        SmartSize[] validSizes = new SmartSize[allSizes.length];
        int target = 0;
        for (int i = 0; i < validSizes.length; i++) {
            Size s = allSizes[i];
            SmartSize ss = new SmartSize(s.getWidth(), s.getHeight());
            validSizes[i] = ss;
            // Then, get the largest output size that is smaller or equal than our max size
            if (validSizes[i].max() <= maxSize.max()
                    && ss.min() <= maxSize.min()) {
                target = i;
                break;
            }
        }
        return validSizes[target].size;
    }

    static SmartSize SIZE_1080P = new SmartSize(1280, 720);

    public static int computeRelativeRotation(
            CameraCharacteristics characteristics,
            int surfaceRotation) {
        int sensorOrientationDegrees =
                characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);

        int deviceOrientationDegrees;
        switch (surfaceRotation) {
            case Surface.ROTATION_90:
                deviceOrientationDegrees = 90;
                break;
            case Surface.ROTATION_180:
                deviceOrientationDegrees = 180;
                break;
            case Surface.ROTATION_270:
                deviceOrientationDegrees = 270;
                break;
            default:
                deviceOrientationDegrees = 0;
                break;
        }

        // Reverse device orientation for front-facing cameras
        int sign = 0;
        if (characteristics.get(CameraCharacteristics.LENS_FACING) ==
                CameraCharacteristics.LENS_FACING_FRONT)
            sign = 1;
        else sign = -1;

        // Calculate desired JPEG orientation relative to camera orientation to make
        // the image upright relative to the device orientation
        return (sensorOrientationDegrees - (deviceOrientationDegrees * sign) + 360) % 360;
    }

    /**
     * 检测剩余的存储大小
     *
     * @param tfCard tf card root path
     * @return the space of internal storage or tf card
     */
    public static long getLeftSpace(String tfCard) {
        long space = 0;
        if (TextUtils.isEmpty(tfCard)) {
            StorageUtil.Storage s = StorageUtil.fetchStorage();
            return s.availableSize / 1024;
        }
        return space;
    }

    /**
     * 删除录制文件，释放一定的空间
     *
     * @param dir    视频目录
     * @param number 要删除的文件数量
     */
    public static void clearWith(String dir, int number) {
        File file = new File(dir);
        if (!file.exists()) return;

        // 过滤最旧的视频文件
        File[] bad = file.listFiles();
        if (bad != null) {
            Arrays.sort(bad, (f1, f2) -> (int) (f1.lastModified() - f2.lastModified()));
            int count = 0;
            for (File f : bad) {
                count++;
                if (count > number) break;
                f.delete();
            }
            Log.d("Storage", "delete files:" + count);
        }
    }
}

/**
 * Helper class used to pre-compute shortest and longest sides of a [Size]
 */
class SmartSize {
    public final Size size;

    public SmartSize(int x, int y) {
        size = new Size(x, y);
    }

    public int width() {
        return size.getWidth();
    }

    public int height() {
        return size.getHeight();
    }

    public int max() {
        return Math.max(size.getWidth(), size.getHeight());
    }

    public int min() {
        return Math.min(size.getWidth(), size.getHeight());
    }

    @Override
    public String toString() {
        return "SmartSize{" + max() + "x" + min() + "}";
    }
}