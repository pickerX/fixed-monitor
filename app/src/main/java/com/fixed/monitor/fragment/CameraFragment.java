package com.fixed.monitor.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.fixed.monitor.R;
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
                //.setDuration(1) 1分钟录制时长，测试用
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
            public void onStopped(long stopMillis) {
                updateStateText("录制结束, 即将开始下一次录制...");
            }
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
