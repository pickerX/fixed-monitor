package com.fixed.monitor.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.fixed.monitor.util.VideoPathUtil;
import com.lib.camera.CameraLifecycle;
import com.lib.camera.CameraUtils;
import com.lib.camera.view.AutoFitSurfaceView;
import com.lib.record.Config;
import com.lib.record.ConfigBuilder;
import com.lib.record.Monitor;
import com.lib.record.MonitorFactory;

/**
 * @author pickerx
 * @date 2022/2/4 9:25 上午
 */
public class CameraFragment extends Fragment {

    private Monitor monitor;
    private AutoFitSurfaceView mAutoFitSurfaceView;
    private TextView mStateTextView;

    private IVideoRecordDao dao;//

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera, null, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAutoFitSurfaceView = view.findViewById(R.id.view_finder);
        mStateTextView = view.findViewById(R.id.tv_state);

        dao = new VideoRecordDaoImpl(getContext());//初始化数据库dao
        view.findViewById(R.id.playback_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), VideoListAct.class));
            }
        });

        view.findViewById(R.id.setting_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), SettingAct.class));
            }
        });

        String directory = VideoPathUtil.getPath(requireContext());
        // 未设置路径时，采用默认路径
        if (TextUtils.isEmpty(directory)) {
            directory = Monitor.DEFAULT_SAVE_DIR;

            String path = CameraUtils.getDefaultDir(requireContext(), Monitor.DEFAULT_SAVE_DIR)
                    .getAbsolutePath();
            VideoPathUtil.savePath(requireContext(), path);
        }

        Config config = new ConfigBuilder(requireContext())
                .setDirectory(VideoPathUtil.getPath(requireContext()))
                .setCameraOrientation(Monitor.FACING_FRONT)
                .setTarget(mAutoFitSurfaceView)
                .setDirectory(directory)
                .setPreview(true)
                .setLoop(true)
                .build();

        monitor = MonitorFactory.getInstance().create(config);
        monitor.setStateCallback(new CameraLifecycle() {
            @Override
            public void onPrepared() {
                updateStateText("已准备...");
            }

            @Override
            public void onStarted(long startMillis) {
                updateStateText("录制中...");
            }

            @Override
            public void onStopped(long stopMillis, String name, String path, long size,long duringTime) {
                updateStateText("录制结束, 即将开始下一次录制...");
                try {
                    //视频录制结束插入表
                    VideoRecordBean videoRecordBean = new VideoRecordBean();
                    videoRecordBean.videoName = name;
                    videoRecordBean.videoCachePath = path;
                    videoRecordBean.videoSize = size;
                    videoRecordBean.videoCover = "";
                    videoRecordBean.videoDuringTime = duringTime;
                    dao.insert(videoRecordBean);
                }catch (Exception e){

                }
            }

//            @Override
//            public void onStopped(long stopMillis) {
//
//            }
        });
        monitor.recordNow(requireContext());
    }

    private void updateStateText(String content) {
        mStateTextView.post(() -> {
            mStateTextView.setText(content);
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        monitor.release();
    }
}
