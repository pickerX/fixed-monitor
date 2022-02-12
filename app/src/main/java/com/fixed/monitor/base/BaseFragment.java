package com.fixed.monitor.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.fixed.monitor.R;

public abstract class BaseFragment extends Fragment {

    boolean isActive = false;
    boolean fristLoad = true;

    /**
     * @param
     * @return
     * @description
     * @author jiejack
     * @time 2022/2/2 8:34 下午
     */
    public abstract int setLayoutID();


    /**
     * @param
     * @return
     * @description 处理view
     * @author jiejack
     * @time 2022/2/2 8:34 下午
     */
    public abstract void initView( View view);


    /**
     * @param
     * @return
     * @description 业务逻辑
     * @author jiejack
     * @time 2022/2/2 8:34 下午
     */
    public abstract void doBusiness();

    /**
     * @param
     * @return
     * @description Act唤醒
     * @author jiejack
     * @time 2022/2/2 8:34 下午
     */
    public abstract void doWeakUp();


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(setLayoutID(), null, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        initView(view);
        doBusiness();
    }

    @Override
    public void onResume() {
        super.onResume();
        isActive = true;
        if (!fristLoad) {
            doWeakUp();
        }
        fristLoad = false;
    }

    @Override
    public void onPause() {
        super.onPause();
        isActive = false;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        isActive = !hidden;
    }
}
