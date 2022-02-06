package com.fixed.monitor;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentContainerView;

import com.fixed.monitor.model.popup.PopupInputPswView;
import com.fixed.monitor.model.setting.SettingAct;
import com.fixed.monitor.model.video.VideoListAct;

public class MainActivity extends AppCompatActivity {

    PopupInputPswView popupInputPswView;
    private FragmentContainerView mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFragment = findViewById(R.id.fragment_container);
        popupInputPswView = new PopupInputPswView(MainActivity.this, new PopupInputPswView.PopupInputPswViewInterface() {
            @Override
            public void success() {

            }
        });
//        findViewById(R.id.test_tv).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
////              VideoPlayerAct.openAct(MainActivity.this, DataUtil.SAMPLE_URL);
//                startActivity(new Intent(MainActivity.this, VideoListAct.class));
////                popupInputPswView.showCenter(findViewById(R.id.test_tv));
//            }
//        });
//        findViewById(R.id.settting_tv).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
////              VideoPlayerAct.openAct(MainActivity.this, DataUtil.SAMPLE_URL);
//                startActivity(new Intent(MainActivity.this, SettingAct.class));
//            }
//        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                popupInputPswView.showCenter(findViewById(R.id.fragment_container));
            }
        }, 300);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (popupInputPswView != null && popupInputPswView.isShow()) {
            return false;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // request full screen
        mFragment.post(() -> mFragment.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LOW_PROFILE |
                        View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY));
    }
}