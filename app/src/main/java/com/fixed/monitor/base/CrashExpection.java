package com.fixed.monitor.base;

import static android.os.Environment.DIRECTORY_DOCUMENTS;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;


import com.fixed.monitor.model.App;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class CrashExpection implements UncaughtExceptionHandler {

	public static final String TAG = "HCCrashExpection";
	private static CrashExpection instance;
	private Context mContext;
	private UncaughtExceptionHandler mDefaultHandler;
	// 用来存储设备信息和异常信息
	private Map<String, String> infos = new HashMap<String, String>();
	private SimpleDateFormat formatter = new SimpleDateFormat(
			"yyyy-MM-dd-HH-mm-ss");

	private String path;

	private CrashExpection(Context context) {
		init(context);
	}

	public static CrashExpection getInstance(Context context) {
		if (instance == null) {
			instance = new CrashExpection(context);
		}
		return instance;
	}

	private void init(Context context) {
		this.path = getCachePathFile(File.separator + "crash").getAbsolutePath();
		mContext = context;
		mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(this);
	}

	public String getPath() {
		return path;
	}

	@Override
	public void uncaughtException(Thread thread, final Throwable ex) {
		ex.printStackTrace();
		if (!handleException(ex) && mDefaultHandler != null) {
//			try {
//				Thread.sleep(5000);
//			} catch (InterruptedException e) {
//				Log.e(TAG, "error : ", e);
//			}
			// 用来存储设备信息和异常信息
			mDefaultHandler.uncaughtException(thread, ex);
		} else {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				Log.e(TAG, "error : ", e);
			}
			// 退出程序
			android.os.Process.killProcess(android.os.Process.myPid());
			System.exit(0);
		}
	}

	/**
	 *  自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
	 *
	 * @param ex
	 * @return true:如果处理了该异常信息;否则返回false.
	 */
	private boolean handleException(Throwable ex) {
		if (ex == null) {
			return false;
		}
		// 使用Toast来显示异常信息
		new Thread() {
			@Override
			public void run() {
				Looper.prepare();
//				Toast.makeText(mContext, "崩溃信息上传中", Toast.LENGTH_LONG).show();
				Looper.loop();
			}
		}.start();
		// 收集设备参数信息
		collectDeviceInfo(mContext);
		// 保存日志文件
		saveCrashInfo2File(ex);
		return false;
	}

	/**
	 * 收集设备参数信息
	 *
	 * @param ctx
	 */
	public void collectDeviceInfo(Context ctx) {
		try {
			PackageManager pm = ctx.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(),
					PackageManager.GET_ACTIVITIES);
			if (pi != null) {
				String versionName = pi.versionName == null ? "null"
						: pi.versionName;
				String versionCode = pi.versionCode + "";
				infos.put("versionName", versionName);
				infos.put("versionCode", versionCode);
			}
		} catch (NameNotFoundException e) {
			Log.e(TAG, "an error occured when collect package info", e);
		}
		Field[] fields = Build.class.getDeclaredFields();
		for (Field field : fields) {
			try {
				field.setAccessible(true);
				infos.put(field.getName(), field.get(null).toString());
				Log.d(TAG, field.getName() + " : " + field.get(null));
			} catch (Exception e) {
				Log.e(TAG, "an error occured when collect crash info", e);
			}
		}
	}

	/**
	 * 保存错误信息到文件中
	 *
	 * @param ex
	 * @return 返回文件名称,便于将文件传送到服务器
	 */
	private void saveCrashInfo2File(Throwable ex) {

		StringBuffer sb = new StringBuffer();
		for (Map.Entry<String, String> entry : infos.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			sb.append(key + "=" + value + "\n");
		}

		Writer writer = new StringWriter();
		PrintWriter printWriter = new PrintWriter(writer);
		ex.printStackTrace(printWriter);
		Throwable cause = ex.getCause();
		while (cause != null) {
			cause.printStackTrace(printWriter);
			cause = cause.getCause();
		}
		printWriter.close();
		String result = writer.toString();
		sb.append(result);
		try {
			long timestamp = System.currentTimeMillis();
			String time = formatter.format(new Date());
			String fileName = "crash-" + time + "-" + timestamp + ".txt";
			File dir = new File(path);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			File file = new File(path + "/" + fileName);
			if (file != null) {
				if (!file.exists()) {
					file.createNewFile();
				}
				FileOutputStream fos = new FileOutputStream(path + "/" + fileName);
				fos.write(sb.toString().getBytes());
				fos.close();
			}
//			sendtoService(file);
			return;
		} catch (Exception e) {
			Log.e(TAG, "an error occured while writing file...", e);
		}
		return;
	}

	/***
	 * 获取缓存路径
	 *
	 * @return
	 */
	public static File getCachePathFile(String path) {
		String status = Environment.getExternalStorageState();
		File dir = null;
		if (status.equals(Environment.MEDIA_MOUNTED)) {
//			dir = new File(App.getApp().getExternalFilesDir(DIRECTORY_DOCUMENTS)
			dir = new File(Environment.getExternalStorageDirectory()
					+ File.separator + "comfixedmonitor" + File.separator + path);
			if (!dir.exists()) {
				dir.mkdirs();
			}

		} else {
			dir = new File(Environment.getDownloadCacheDirectory()
					+ File.separator + "comfixedmonitor" + File.separator + path);
			if (!dir.exists()) {
				dir.mkdirs();
			}
		}
		return dir;
	}
}
