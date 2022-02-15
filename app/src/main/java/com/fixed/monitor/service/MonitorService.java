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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.fixed.monitor.R;
import com.fixed.monitor.bean.VideoRecordBean;
import com.fixed.monitor.model.dbdao.IVideoRecordDao;
import com.fixed.monitor.model.dbdao.impl.VideoRecordDaoImpl;
import com.fixed.monitor.util.CEvent;
import com.fixed.monitor.util.MeasureUtil;
import com.fixed.monitor.util.ToolUtil;
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

    private boolean isRecording;
    private long recordDuringTime;
    private long startMillis;
    private IVideoRecordDao dao;
    private ScheduledThreadPoolExecutor mCountDownExecutor;

    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mWindowManagerParams;
    private int smallWidth, smallHeight;
    private View windowMainView;
    private AutoFitSurfaceView mAutoFitSurfaceView;
    private View record_tv, record_view;
    private TextView state_tv;

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

        smallWidth = MeasureUtil.dip2px(this, 300);
        smallHeight = MeasureUtil.dip2px(this, 200);

        initSqlDao();
        initWindowMangerView();
        initMonitorRecord();
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

//        mAutoFitSurfaceView = new AutoFitSurfaceView(this);
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
                    videoRecordBean.videoCreateTime = System.currentTimeMillis();
                    videoRecordBean.videoRecordStartTime = stopMillis - duringTime;
                    videoRecordBean.videoRecordEndTime = stopMillis;
                    dao.insert(videoRecordBean);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                isRecording = false;

                if (windowMainView != null)
                    windowMainView.post(new Runnable() {
                        @Override
                        public void run() {
                            if (record_tv != null) {
                                record_tv.setVisibility(View.GONE);
                            }
                            if (record_view != null) {
                                record_view.setVisibility(View.GONE);
                            }
                            if (state_tv != null) {
                                state_tv.setText("录制暂停");
                            }
                        }
                    });
//                EventBus.getDefault().post(new CEvent.MonitorRecordingEvent("录制暂停", 0));
            }
        });
    }

    public void initWindowMangerView() {
        windowMainView = LayoutInflater.from(this).inflate(R.layout.view_window_crameview, null);
        record_view = windowMainView.findViewById(R.id.record_view);
        record_tv = windowMainView.findViewById(R.id.record_tv);
        state_tv = windowMainView.findViewById(R.id.state_tv);
        mAutoFitSurfaceView = windowMainView.findViewById(R.id.view_finder);
        //设置WindowManger布局参数以及相关属性
        mWindowManagerParams = new WindowManager.LayoutParams(
                smallWidth,
                smallHeight,
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
        mAutoFitSurfaceView.setMaxSize(smallWidth, smallHeight);
        //获取WindowManager对象
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mWindowManager.addView(windowMainView, mWindowManagerParams);

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
                    if (windowMainView != null)
                        windowMainView.post(new Runnable() {
                            @Override
                            public void run() {
                                if (record_tv != null) {
                                    record_tv.setVisibility(View.VISIBLE);
                                }
                                if (record_view != null) {
                                    record_view.setVisibility(recordDuringTime % 2 == 0 ? View.VISIBLE : View.INVISIBLE);
                                }
                                if (state_tv != null) {
                                    state_tv.setText("录制中:" + ToolUtil.secondToTime(recordDuringTime));
                                }
                            }
                        });
//                    EventBus.getDefault().post(new CEvent.MonitorRecordingEvent("录制中", recordDuringTime));
                }
            }
        }, 0, 1, TimeUnit.SECONDS);//0表示首次执行任务的延迟时间，40表示每次执行任务的间隔时间，TimeUnit.MILLISECONDS执行的时间间隔数值单位
    }


    public void bindMonitorView(View parentView) {
        parentView.post(new Runnable() {
            @Override
            public void run() {
//                parentView.measure(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
                int[] points = new int[2];
                parentView.getLocationOnScreen(points);
                mWindowManagerParams.x = points[0];
                mWindowManagerParams.y = points[1];
                mWindowManagerParams.gravity = Gravity.TOP | Gravity.LEFT;
                mWindowManagerParams.width = parentView.getWidth();
                mWindowManagerParams.height = parentView.getHeight();
                mAutoFitSurfaceView.setMaxSize(MeasureUtil.getScreenWidth(MonitorService.this), MeasureUtil.getScreenHeight(MonitorService.this));
                mWindowManager.updateViewLayout(windowMainView, mWindowManagerParams);
            }
        });
    }


    public void unBindMonitorView(ViewGroup viewfl) {
        mWindowManagerParams.x = MeasureUtil.dip2px(this,98);
        mWindowManagerParams.y = 100;
        mWindowManagerParams.gravity = Gravity.BOTTOM | Gravity.LEFT;
        mWindowManagerParams.width = smallWidth;
        mWindowManagerParams.height = smallHeight;
        mAutoFitSurfaceView.setMaxSize(smallWidth, smallHeight);
        mWindowManager.updateViewLayout(windowMainView, mWindowManagerParams);
    }

    public void refreshRecordUI() {

    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mWindowManager != null && windowMainView != null) {
            mWindowManager.removeView(windowMainView);
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