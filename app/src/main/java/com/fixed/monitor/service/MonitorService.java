package com.fixed.monitor.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

import com.fixed.monitor.bean.VideoRecordBean;
import com.fixed.monitor.model.dbdao.IVideoRecordDao;
import com.fixed.monitor.model.dbdao.impl.VideoRecordDaoImpl;
import com.fixed.monitor.util.CEvent;
import com.fixed.monitor.util.MeasureUtil;
import com.fixed.monitor.util.VideoPathUtil;
import com.lib.camera.CameraLifecycle;
import com.lib.camera.CameraUtils;
import com.lib.camera.view.AutoFitSurfaceView;
import com.lib.record.Config;
import com.lib.record.ConfigBuilder;
import com.lib.record.Monitor;
import com.lib.record.MonitorFactory;

import org.greenrobot.eventbus.EventBus;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MonitorService extends Service {
    public static final String TAG = "MonitorService";
    public String notificationId = "Monitora_service_id";
    public String notificationName = "录像服务";
    private MonitorServiceBinder mBinder;

    private Monitor monitor;
    private AutoFitSurfaceView mAutoFitSurfaceView;
    private boolean isRecording;
    private long recordDuringTime;
    private long startMillis;
    private IVideoRecordDao dao;
    private ScheduledThreadPoolExecutor mCountDownExecutor;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mWindowManagerParams;

    private static final Intent SERVICE_INTENT = new Intent();

    static {
        SERVICE_INTENT.setComponent(new ComponentName("com.fixed.monitor", "com.fixed.monitor.service.MonitorService"));
    }

    public static Intent getIntent() {
        return SERVICE_INTENT;
    }

    public static Intent getIntent(Context context) {
        Intent intent = new Intent(context, MonitorService.class);
        return intent;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    // Binder
    public class MonitorServiceBinder extends Binder {
        public MonitorService getService() {
            return MonitorService.this;
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notificationChannel = new NotificationChannel(notificationId,
                        notificationName, NotificationManager.IMPORTANCE_HIGH);
                notificationChannel.enableLights(true);
                notificationChannel.setLightColor(Color.RED);
                notificationChannel.setShowBadge(true);
                notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                manager.createNotificationChannel(notificationChannel);
            }
            Notification notification = new Notification.Builder(this).setChannelId(notificationId)
                    .getNotification();
            notification.flags |= Notification.FLAG_AUTO_CANCEL;
            startForeground(471542, notification);
        }
        mBinder = new MonitorServiceBinder();

        initSqlDao();
        initMonitorRecord();
        //设置WindowManger布局参数以及相关属性
        mWindowManagerParams = new WindowManager.LayoutParams(
                MeasureUtil.dip2px(this, 120),
                MeasureUtil.dip2px(this, 60),
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        //初始化位置
        mWindowManagerParams.gravity = Gravity.BOTTOM | Gravity.RIGHT;
        mWindowManagerParams.x = 10;
        mWindowManagerParams.y = 100;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mWindowManagerParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            mWindowManagerParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        mAutoFitSurfaceView.setMaxSize(MeasureUtil.dip2px(this, 120), MeasureUtil.dip2px(this, 60));
        //获取WindowManager对象
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mWindowManager.addView(mAutoFitSurfaceView, mWindowManagerParams);


        startCountDown();
        monitor.recordNow(this);
    }

    private void initSqlDao() {
        dao = new VideoRecordDaoImpl(this);//初始化数据库dao
    }

    public void initMonitorRecord() {
        String directory = VideoPathUtil.getPath(this);
        // 未设置路径时，采用默认路径
        if (TextUtils.isEmpty(directory)) {
            directory = Monitor.DEFAULT_SAVE_DIR;
            String path = CameraUtils.getDefaultDir(this, Monitor.DEFAULT_SAVE_DIR)
                    .getAbsolutePath();
            VideoPathUtil.savePath(this, path);
        }

        mAutoFitSurfaceView = new AutoFitSurfaceView(this);
        Config config = new ConfigBuilder(this)
                .setDirectory(VideoPathUtil.getPath(this))
                .setCameraOrientation(Monitor.FACING_FRONT)
                .setTarget(mAutoFitSurfaceView)
//                .setDuration(VideoPathUtil.getVideoRecordTime(this))
                .setDuration(1) //1分钟录制时长，测试用
                .setDirectory(directory)
                .setPreview(true)
                .setLoop(true)
                .build();

        monitor = MonitorFactory.getInstance().create(config);
        monitor.setStateCallback(new CameraLifecycle() {
            @Override
            public void onPrepared() {

            }

            @Override
            public void onStarted(long startMillis) {
                isRecording = true;
                MonitorService.this.startMillis = startMillis;
                MonitorService.this.recordDuringTime = recordDuringTime;
            }

            @Override
            public void onStopped(long stopMillis, String name, String path, long size, long duringTime, String coverPath) {
                try {
                    //视频录制结束插入表
                    VideoRecordBean videoRecordBean = new VideoRecordBean();
                    videoRecordBean.videoName = name;
                    videoRecordBean.videoCachePath = path;
                    videoRecordBean.videoSize = size;
                    videoRecordBean.videoCover = coverPath;
                    videoRecordBean.videoDuringTime = duringTime;
                    dao.insert(videoRecordBean);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                isRecording = false;
                EventBus.getDefault().post(new CEvent.MonitorRecordingEvent("录制暂停", 0));
            }
        });
    }

    public void startCountDown() {
        if (null != mCountDownExecutor) {
            mCountDownExecutor.shutdownNow();
        }
        mCountDownExecutor = new ScheduledThreadPoolExecutor(1);
        mCountDownExecutor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (isRecording) {
                    recordDuringTime++;
                    EventBus.getDefault().post(new CEvent.MonitorRecordingEvent("录制中", recordDuringTime));
                }
            }
        }, 0, 1, TimeUnit.SECONDS);//0表示首次执行任务的延迟时间，40表示每次执行任务的间隔时间，TimeUnit.MILLISECONDS执行的时间间隔数值单位
    }


    public void bindMonitorView(ViewGroup viewGroup) {
        if (mAutoFitSurfaceView != null) {
            try {
                mWindowManager.removeView(mAutoFitSurfaceView);
            } catch (Exception e) {
            }
            if(mAutoFitSurfaceView.getParent()!=null){
                try {
                    ((ViewGroup)mAutoFitSurfaceView.getParent()).removeView(mAutoFitSurfaceView);
                }catch (Exception e){
                }
            }
            mAutoFitSurfaceView.setMaxSize(0, 0);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        viewGroup.addView(mAutoFitSurfaceView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                    }catch (Exception e){
                    }
                }
            },500);

        }
    }


    public void unBindMonitorView(ViewGroup viewfl) {
        if (mAutoFitSurfaceView != null) {
            if (viewfl != null) {
                viewfl.removeAllViews();
            }
            if(mAutoFitSurfaceView.getParent()!=null){
                try {
                    ((ViewGroup)mAutoFitSurfaceView.getParent()).removeView(mAutoFitSurfaceView);
                }catch (Exception e){
                }
            }
            mAutoFitSurfaceView.setMaxSize(MeasureUtil.dip2px(this, 120), MeasureUtil.dip2px(this, 60));
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        mWindowManager.addView(mAutoFitSurfaceView, mWindowManagerParams);
                    }catch (Exception e){
                    }
                }
            },500);
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mWindowManager != null && mAutoFitSurfaceView != null) {
            mWindowManager.removeView(mAutoFitSurfaceView);
        }
        if (monitor != null) {
            monitor.release();
        }
        isRecording = false;
        if (mCountDownExecutor != null) {
            mCountDownExecutor.shutdownNow();
            mCountDownExecutor.shutdown();
        }
    }
}
