package com.fixed.monitor.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.fixed.monitor.MainActivity;
import com.fixed.monitor.R;
import com.fixed.monitor.bean.VideoRecordBean;
import com.fixed.monitor.model.dbdao.IVideoRecordDao;
import com.fixed.monitor.model.dbdao.impl.VideoRecordDaoImpl;
import com.fixed.monitor.model.setting.SettingAct;
import com.fixed.monitor.model.video.VideoListAct;
import com.fixed.monitor.util.CEvent;
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
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author pickerx
 * @date 2022/2/4 9:25 上午
 */
public class CameraFragment extends Fragment {

    //    private Monitor monitor;
//    private AutoFitSurfaceView mAutoFitSurfaceView;
    private TextView mStateTextView;
    private FrameLayout viewfl;

//    private IVideoRecordDao dao;//
//    private int countTime = 0;
//    private ScheduledThreadPoolExecutor mCountDownExecutor;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera, null, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        mAutoFitSurfaceView = view.findViewById(R.id.view_finder);
        viewfl = view.findViewById(R.id.viewfl);
        mStateTextView = view.findViewById(R.id.tv_state);

//        dao = new VideoRecordDaoImpl(getContext());//初始化数据库dao
//        view.findViewById(R.id.playback_tv).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                startActivity(new Intent(getContext(), VideoListAct.class));
//            }
//        });
//
//        view.findViewById(R.id.setting_tv).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                startActivity(new Intent(getContext(), SettingAct.class));
//            }
//        });

        String directory = VideoPathUtil.getPath(requireContext());
        // 未设置路径时，采用默认路径
        if (TextUtils.isEmpty(directory)) {
            directory = Monitor.DEFAULT_SAVE_DIR;

            String path = CameraUtils.getDefaultDir(requireContext(), Monitor.DEFAULT_SAVE_DIR)
                    .getAbsolutePath();
            VideoPathUtil.savePath(requireContext(), path);
        }

//        Config config = new ConfigBuilder(requireContext())
//                .setDirectory(VideoPathUtil.getPath(requireContext()))
//                .setCameraOrientation(Monitor.FACING_FRONT)
//                .setTarget(mAutoFitSurfaceView)
//                .setDuration(VideoPathUtil.getVideoRecordTime(getContext()))
//                //.setDuration(1) 1分钟录制时长，测试用
//                .setDirectory(directory)
//                .setPreview(true)
//                .setLoop(true)
//                .build();
//
//        monitor = MonitorFactory.getInstance().create(config);
//        monitor.setStateCallback(new CameraLifecycle() {
//            @Override
//            public void onPrepared() {
//                updateStateText("已准备...");
//            }
//
//            @Override
//            public void onStarted(long startMillis) {
////                updateStateText("录制中...");
//                startCountDown();
//            }
//
//            @Override
//            public void onStopped(long stopMillis, String name, String path, long size, long duringTime, String coverPath) {
//                if (mCountDownExecutor != null) {
//                    mCountDownExecutor.shutdownNow();
//                    mCountDownExecutor.shutdown();
//                }
//                updateStateText("录制结束, 即将开始下一次录制...");
//                try {
//                    //视频录制结束插入表
//                    VideoRecordBean videoRecordBean = new VideoRecordBean();
//                    videoRecordBean.videoName = name;
//                    videoRecordBean.videoCachePath = path;
//                    videoRecordBean.videoSize = size;
//                    videoRecordBean.videoCover = coverPath;
//                    videoRecordBean.videoDuringTime = duringTime;
//                    dao.insert(videoRecordBean);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//        monitor.recordNow(requireContext());

        EventBus.getDefault().register(this);

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                if (((MainActivity) getContext()).monitorService != null) {
//                    ((MainActivity) getContext()).monitorService.bindMonitorView(viewfl);
//                }
//            }
//        }, 1000);
    }

    @Override
    public void onResume() {
        super.onResume();
//        viewfl.removeAllViews();
        if (((MainActivity) getContext()).monitorService != null) {
            ((MainActivity) getContext()).monitorService.bindMonitorView(viewfl);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
//        viewfl.removeAllViews();
        if (((MainActivity) getContext()).monitorService != null) {
            ((MainActivity) getContext()).monitorService.unBindMonitorView(viewfl);
        }
    }

    //    public void startCountDown() {
//        if (null != mCountDownExecutor) {
//            mCountDownExecutor.shutdownNow();
//        }
//        countTime = VideoPathUtil.getVideoRecordTime(getContext()) * 60;
//        if (countTime == 0) {
//            return;
//        }
//        mCountDownExecutor = new ScheduledThreadPoolExecutor(1);
//        mCountDownExecutor.scheduleAtFixedRate(new Runnable() {
//            @Override
//            public void run() {
////                loge("time:");
//                updateStateText("录制中,剩余：" + countTime + "秒");
//                countTime--;
//            }
//        }, 0, 1, TimeUnit.SECONDS);//0表示首次执行任务的延迟时间，40表示每次执行任务的间隔时间，TimeUnit.MILLISECONDS执行的时间间隔数值单位
//    }


//    private void updateStateText(String content) {
//        mStateTextView.post(() -> {
//            mStateTextView.setText(content);
//        });
//    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doRecordUpdate(CEvent.MonitorRecordingEvent event) {
        mStateTextView.setText(event.recordStatus + ":" + ToolUtil.secondToTime(event.recordDuringTime));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        viewfl.removeAllViews();
        EventBus.getDefault().unregister(this);
//        if (mCountDownExecutor != null) {
//            mCountDownExecutor.shutdownNow();
//            mCountDownExecutor.shutdown();
//        }
//        monitor.release();
    }
}
