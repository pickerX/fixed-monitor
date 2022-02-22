package com.lib.util;

import android.os.Environment;
import android.os.StatFs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * @author pickerx
 * @date 2022/2/7 12:13 下午
 */
public class StorageUtil {

    public static class Storage {
        public long blockCount;
        public long blockSize;
        public long availableCount;
        public long freeBlocks;
        public long totalSize;
        public long availableSize;
    }

    public static void writeSDFile(String fileName, String content) {
        File file = new File(fileName);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file, true);
            content += "\n";
            byte[] bytes = content.getBytes();
            fos.write(bytes);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Storage fetchStorage() {
        StatFs statFs = new StatFs(Environment.getExternalStorageDirectory().getAbsolutePath());
        // 存储块总数量
        long blockCount = statFs.getBlockCountLong();
        // 块大小
        long blockSize = statFs.getBlockSizeLong();
        // 可用块数量
        long availableCount = statFs.getAvailableBlocksLong();
        // 剩余块数量，注：这个包含保留块（including reserved blocks）即应用无法使用的空间
        long freeBlocks = statFs.getFreeBlocksLong();
        // 这两个方法是直接输出总内存和可用空间，也有getFreeBytes
        // API level 18（JELLY_BEAN_MR2）引入
        long totalSize = statFs.getTotalBytes();
        long availableSize = statFs.getAvailableBytes();
        Storage s = new Storage();
        s.availableCount = availableCount;
        s.blockCount = blockCount;
        s.blockSize = blockSize;
        s.freeBlocks = freeBlocks;
        s.totalSize = totalSize;
        s.availableSize = availableSize;

//        Log.d("statfs", "total = " + getUnit(totalSize));
//        Log.d("statfs", "availableSize = " + getUnit(availableSize));
//
//        //这里可以看出 available 是小于 free ,free 包括保留块。
//        Log.d("statfs", "total = " + getUnit(blockSize * blockCount));
//        Log.d("statfs", "available = " + getUnit(blockSize * availableCount));
//        Log.d("statfs", "free = " + getUnit(blockSize * freeBlocks));
        return s;
    }

    private static final List<String> units = Arrays.asList("B", "KB", "MB", "GB", "TB");

    /**
     * 单位转换
     */
    private static String getUnit(float size) {
        int index = 0;
        while (size > 1024 && index < 4) {
            size = size / 1024;
            index++;
        }
        return String.format(Locale.getDefault(), " %.2f %s", size, units.get(index));
    }
}
