package com.lib.record;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import com.lib.camera.CameraLifecycle;
import com.lib.camera.CameraUtils;
import com.lib.camera.XCamera;

/**
 * @author pickerx
 * @date 2022/1/27 8:54 下午
 */
public class MonitorImpl implements Monitor {

    private final Config config;
    private final XCamera mCamera;
    private long recordingStartMillis;

    public MonitorImpl(Config config) {
        this.config = config;
        this.mCamera = new XCamera(config);
        init();
    }

    private static final int MSG_WHAT_TRY_TO_STOP = 100;

    private final Handler counterHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_WHAT_TRY_TO_STOP) tryToStop();
        }
    };

    private void init() {
        CameraLifecycle callback = new CameraLifecycle() {
            @Override
            public void onPrepared() {
                mCamera.start();
                if (mStateCallback != null) mStateCallback.onPrepared();

                long size = CameraUtils.getLeftSpace(null);
                if (size < config.maxReserveSize) {
                    CameraUtils.clearWith(config.directory, 1);
                }
            }

            @Override
            public void onStarted(long startMillis) {
                recordingStartMillis = startMillis;
                // 每分钟检测录制时间
                counterHandler.sendEmptyMessageDelayed(MSG_WHAT_TRY_TO_STOP, 60 * 1000);
                if (mStateCallback != null) mStateCallback.onStarted(recordingStartMillis);
            }

            @Override
            public void onStopped(long stopMillis, String name, String path, long size,long duringTime,String coverPath) {
                if (mStateCallback != null)
                    mStateCallback.onStopped(stopMillis,name,path,size,duringTime,coverPath);
                if (config.loop) {
                    dispatchNext();
                }
            }

            @Override
            public void lifeLog(int type, String msg, long time) {
                if (mStateCallback != null)
                    mStateCallback.lifeLog(type,msg,time);
            }

            @Override
            public void lifeErro(String msg, long time, Exception e) {
                if(mStateCallback!=null)
                    mStateCallback.lifeErro(msg,time,e);
            }
        };
        this.mCamera.bindLifecycle(callback);
    }

    @Override
    public void recordNow(Context context) {
        mCamera.prepare(context);
    }

    @Override
    public void stop() {
        mCamera.stop();
    }

    private void tryToStop() {
        // Requires recording of at least MIN_REQUIRED_RECORDING_TIME_MILLIS
        long elapsedTimeMillis = (System.currentTimeMillis() - recordingStartMillis) / 1000;
        if (elapsedTimeMillis < config.duration * 60L) {
            counterHandler.sendEmptyMessageDelayed(MSG_WHAT_TRY_TO_STOP, 60 * 1000);
            return;
        }
        mCamera.stop();
    }

    private void dispatchNext() {
        mCamera.restart();
    }

    @Override
    public void release() {
        mCamera.release();
    }

    private CameraLifecycle mStateCallback;

    @Override
    public void setStateCallback(CameraLifecycle callback) {
        this.mStateCallback = callback;
    }

}
