package com.fixed.monitor.service;

import android.app.Activity;
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
import android.os.IBinder;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.camera.view.PreviewView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fixed.monitor.R;
import com.fixed.monitor.base.CrashExpection;
import com.fixed.monitor.base.adapter.MCommAdapter;
import com.fixed.monitor.base.adapter.MCommVH;
import com.fixed.monitor.bean.LifeLogBean;
import com.fixed.monitor.bean.VideoRecordBean;
import com.fixed.monitor.model.App;
import com.fixed.monitor.model.dbdao.IVideoRecordDao;
import com.fixed.monitor.model.dbdao.impl.VideoRecordDaoImpl;
import com.fixed.monitor.util.MeasureUtil;
import com.fixed.monitor.util.ToolUtil;
import com.fixed.monitor.util.VideoPathUtil;
import com.lib.camera.CameraLifecycle;
import com.lib.camera.CameraUtils;
import com.lib.record.Config;
import com.lib.record.ConfigBuilder;
import com.lib.record.Monitor;
import com.lib.record.MonitorFactory;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MonitorService extends Service {
    public static final String TAG = "MonitorService";
    public String notificationId = "Monitora_service_id";
    public String notificationName = "录像服务";
    private MonitorServiceBinder mBinder;

    private Monitor monitor;

    private boolean isRecording;
    private boolean isbind;
    private long recordDuringTime;
    private long startMillis;
    private IVideoRecordDao dao;
    private ScheduledThreadPoolExecutor mCountDownExecutor;

    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mWindowManagerParams;
    private int smallWidth, smallHeight;
    private View windowMainView;
    //    private AutoFitSurfaceView mAutoFitSurfaceView;
    private PreviewView mAutoFitSurfaceView;
    private View record_view;
    private TextView record_tv, state_tv;
    private RecyclerView lifelog_rcv;
    private MCommAdapter<LifeLogBean> lifeLogBeanMCommAdapter;

    private static final Intent SERVICE_INTENT = new Intent();

    static {
        SERVICE_INTENT.setComponent(new ComponentName("com.fixed.monitor", "com.fixed.monitor.service.MonitorService"));
    }

    public static Intent getIntent() {
        return SERVICE_INTENT;
    }

    private Activity viewContext;

    public static Intent getIntent(Context context) {
        Intent intent = new Intent(context, MonitorService.class);
        return intent;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void bindLifecycleOwner(Activity lifecycle) {
        viewContext = lifecycle;
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

        smallWidth = MeasureUtil.dip2px(this, 1);
        smallHeight = MeasureUtil.dip2px(this, 1);

        initSqlDao();
        initWindowMangerView();
        initMonitorRecord();
        startCountDown();
        windowMainView.post(() -> {
            monitor.recordNow(viewContext);
        });

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
                .setDuration(VideoPathUtil.getVideoRecordTime(this))
//                .setDuration(1) //1分钟录制时长，测试用
                .setDirectory(directory)
                .setPreview(true)
                .setLoop(true)
                .setCameraX(true)
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

            @Override
            public void lifeLog(int type, String msg, long time) {
                if (lifelog_rcv != null) {
                    lifelog_rcv.post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                lifeLogBeanMCommAdapter.addOneData(new LifeLogBean(type, msg, "", time));
                                int itemCount = lifeLogBeanMCommAdapter.getItemCount() - 1;
                                lifelog_rcv.smoothScrollToPosition(itemCount);
                            } catch (Exception e) {
                            }
                        }
                    });
                }
            }

            @Override
            public void lifeErro(String msg, long time, Exception e) {
                if (lifelog_rcv != null) {
                    lifelog_rcv.post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                lifeLogBeanMCommAdapter.addOneData(new LifeLogBean(-1, msg, e.getMessage(), time));
                                int itemCount = lifeLogBeanMCommAdapter.getItemCount() - 1;
                                lifelog_rcv.smoothScrollToPosition(itemCount);
                            } catch (Exception e) {
                            }
                        }
                    });
                }
                if (CrashExpection.getInstance(App.getApp()) != null) {
                    CrashExpection.getInstance(App.getApp()).saveExpectionFile(e);
                }
            }
        });
    }

    public void initWindowMangerView() {
        windowMainView = LayoutInflater.from(this).inflate(R.layout.view_window_crameview, null);
        record_view = windowMainView.findViewById(R.id.record_view);
        record_tv = windowMainView.findViewById(R.id.record_tv);
        state_tv = windowMainView.findViewById(R.id.state_tv);
        lifelog_rcv = windowMainView.findViewById(R.id.lifelog_rcv);
        mAutoFitSurfaceView = windowMainView.findViewById(R.id.view_finder);
        //设置WindowManger布局参数以及相关属性
        mWindowManagerParams = new WindowManager.LayoutParams(
                smallWidth,
                smallHeight,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        //初始化位置
//        mWindowManagerParams.gravity = Gravity.BOTTOM | Gravity.RIGHT;
        mWindowManagerParams.x = 10;
        mWindowManagerParams.y = 100;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mWindowManagerParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            mWindowManagerParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        // TODO PreviewView resize
        // mAutoFitSurfaceView.setMaxSize(smallWidth, smallHeight);
        updatePreviewViewSize(smallWidth, smallHeight);
        //获取WindowManager对象
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        record_tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        state_tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);


        lifelog_rcv.setLayoutManager(new LinearLayoutManager(this));
        ((FrameLayout.LayoutParams) lifelog_rcv.getLayoutParams()).width = MeasureUtil.getScreenWidth(this) / 4;
        ((FrameLayout.LayoutParams) lifelog_rcv.getLayoutParams()).height = MeasureUtil.getScreenHeight(this) / 5 * 3;
        lifeLogBeanMCommAdapter = new MCommAdapter<>(this, new MCommVH.MCommVHInterface<LifeLogBean>() {
            @Override
            public int setLayout() {
                return R.layout.view_lifelog_item;
            }

            @Override
            public void bindData(Context context, MCommVH mCommVH, int position, LifeLogBean lifeLogBean) {
                mCommVH.setText(R.id.time_tv, ToolUtil.timestamp2String(lifeLogBean.createTime, "HH:mm:ss"));
                TextView tv = (TextView) mCommVH.getView(R.id.tv);
                switch (lifeLogBean.type) {
                    case 0:
                    default:
                        tv.setTextColor(0xff00FF00);
                        tv.setText(lifeLogBean.msg);
                        break;
                    case -1:
                        tv.setTextColor(0xffFF0000);
                        tv.setText(lifeLogBean.msg + "\n" + "-->" + lifeLogBean.erroMsg);
                        break;
                }

            }
        });
        lifelog_rcv.setAdapter(lifeLogBeanMCommAdapter);
        lifelog_rcv.setVisibility(View.GONE);
        try {
            mWindowManager.addView(windowMainView, mWindowManagerParams);
        } catch (Exception e) {
            e.printStackTrace();
        }

        windowMainView.setOnTouchListener(new View.OnTouchListener() {
            // 触屏监听
            float lastX, lastY;
            int oldOffsetX, oldOffsetY;
            int tag = 0;// 悬浮球 所需成员变量

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (isbind) {
                    return false;
                }
                final int action = event.getAction();
                float x = event.getX();
                float y = event.getY();
                if (tag == 0) {
                    oldOffsetX = mWindowManagerParams.x; // 偏移量
                    oldOffsetY = mWindowManagerParams.y; // 偏移量
                }
                if (action == MotionEvent.ACTION_DOWN) {
                    lastX = x;
                    lastY = y;
                } else if (action == MotionEvent.ACTION_MOVE) {
                    mWindowManagerParams.x += (int) (x - lastX) / 3; // 减小偏移量,防止过度抖动
                    mWindowManagerParams.y += (int) (y - lastY) / 3; // 减小偏移量,防止过度抖动
                    tag = 1;
                    mWindowManager.updateViewLayout(windowMainView, mWindowManagerParams);
                } else if (action == MotionEvent.ACTION_UP) {
                    int newOffsetX = mWindowManagerParams.x;
                    int newOffsetY = mWindowManagerParams.y;
                    // 只要按钮一动位置不是很大,就认为是点击事件
                    if (Math.abs(oldOffsetX - newOffsetX) <= 20
                            && Math.abs(oldOffsetY - newOffsetY) <= 20) {
//                        onFloatViewClick(l);
                    } else {
                        tag = 0;
                    }
                }
                return true;
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
        isbind = true;
        parentView.post(new Runnable() {
            @Override
            public void run() {
//                parentView.measure(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
                int[] points = new int[2];
                parentView.getLocationOnScreen(points);
                mWindowManagerParams.x = points[0];
                mWindowManagerParams.y = points[1];
//                mWindowManagerParams.gravity = Gravity.TOP | Gravity.LEFT;
                mWindowManagerParams.width = parentView.getWidth();
                mWindowManagerParams.height = parentView.getHeight();
                record_tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
                state_tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
                // mAutoFitSurfaceView.setMaxSize(MeasureUtil.getScreenWidth(MonitorService.this), MeasureUtil.getScreenHeight(MonitorService.this));
                updatePreviewViewSize(MeasureUtil.getScreenWidth(MonitorService.this), MeasureUtil.getScreenHeight(MonitorService.this));
                lifelog_rcv.setVisibility(View.VISIBLE);
                mWindowManager.updateViewLayout(windowMainView, mWindowManagerParams);
            }
        });
    }

    private void updatePreviewViewSize(int width, int height) {
        ViewGroup.LayoutParams p = mAutoFitSurfaceView.getLayoutParams();
        p.width = width;
        p.height = height;
        mAutoFitSurfaceView.setLayoutParams(p);
    }

    public void unBindMonitorView(ViewGroup viewfl) {
        mWindowManagerParams.x = MeasureUtil.dip2px(this, 98);
        mWindowManagerParams.y = 100;
//        mWindowManagerParams.gravity = Gravity.BOTTOM | Gravity.LEFT;
        mWindowManagerParams.width = smallWidth;
        mWindowManagerParams.height = smallHeight;
        // mAutoFitSurfaceView.setMaxSize(smallWidth, smallHeight);
        updatePreviewViewSize(smallWidth, smallHeight);
        record_tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        state_tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        lifelog_rcv.setVisibility(View.GONE);
        mWindowManager.updateViewLayout(windowMainView, mWindowManagerParams);

        isbind = false;
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
