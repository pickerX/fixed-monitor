package com.fixed.monitor.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.fixed.monitor.R;
import com.fixed.monitor.util.VideoPathUtil;
import com.lib.record.Config;
import com.lib.record.ConfigBuilder;
import com.lib.record.Monitor;
import com.lib.record.MonitorFactory;
import com.lib.record.view.AutoFitSurfaceView;

/**
 * @author pickerx
 * @date 2022/2/4 9:25 上午
 */
public class CameraFragment extends Fragment {

    private Monitor monitor;
    private AutoFitSurfaceView mAutoFitSurfaceView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera, null, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAutoFitSurfaceView = view.findViewById(R.id.view_finder);
        Log.e("Fragment", "onViewCreated");

        Config config = new ConfigBuilder(requireContext())
                .setDirectory(VideoPathUtil.getPath(requireContext()))
                .setCameraOrientation(Monitor.FACING_FRONT)
                .setTarget(mAutoFitSurfaceView)
                .setPreview(true)
                .build();
        monitor = MonitorFactory.getInstance().create(config);

        monitor.recordNow(requireContext());

    }
}
