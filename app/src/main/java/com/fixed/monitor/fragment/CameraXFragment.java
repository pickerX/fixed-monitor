package com.fixed.monitor.fragment;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.fixed.monitor.util.CEvent;
import com.fixed.monitor.util.ToolUtil;
import com.fixed.monitor.util.VideoPathUtil;
import com.lib.camera.CameraUtils;
import com.lib.record.Monitor;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * @author pickerx
 * @date 2022/2/4 9:25 上午
 */
public class CameraXFragment extends Fragment {

    private TextView mStateTextView;
    private FrameLayout viewfl;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera, null, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewfl = view.findViewById(R.id.viewfl);
        mStateTextView = view.findViewById(R.id.tv_state);

        String directory = VideoPathUtil.getPath(requireContext());
        // 未设置路径时，采用默认路径
        if (TextUtils.isEmpty(directory)) {
            directory = Monitor.DEFAULT_SAVE_DIR;

            String path = CameraUtils.getDefaultDir(requireContext(), Monitor.DEFAULT_SAVE_DIR)
                    .getAbsolutePath();
            VideoPathUtil.savePath(requireContext(), path);
        }
        EventBus.getDefault().register(this);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (((MainActivity) getContext()).monitorService != null) {
            ((MainActivity) getContext()).monitorService.bindMonitorView(viewfl);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (((MainActivity) getContext()).monitorService != null) {
            ((MainActivity) getContext()).monitorService.unBindMonitorView(viewfl);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doRecordUpdate(CEvent.MonitorRecordingEvent event) {
        mStateTextView.setText(event.recordStatus + ":" + ToolUtil.secondToTime(event.recordDuringTime));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        viewfl.removeAllViews();
        EventBus.getDefault().unregister(this);
    }
}
