package com.fixed.monitor;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.view.View;

import com.fixed.monitor.model.popup.PopupInputPswView;
import com.fixed.monitor.model.setting.SettingAct;
import com.fixed.monitor.model.video.VideoListAct;
import com.fixed.monitor.model.video.VideoPlayerAct;
import com.fixed.monitor.util.DataUtil;

public class MainActivity extends AppCompatActivity {

    PopupInputPswView popupInputPswView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        popupInputPswView =  new PopupInputPswView(MainActivity.this, new PopupInputPswView.PopupInputPswViewInterface() {
            @Override
            public void success() {

            }
        });
        findViewById(R.id.test_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//              VideoPlayerAct.openAct(MainActivity.this, DataUtil.SAMPLE_URL);
                startActivity(new Intent(MainActivity.this, VideoListAct.class));
//                popupInputPswView.showCenter(findViewById(R.id.test_tv));
            }
        });
        findViewById(R.id.settting_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//              VideoPlayerAct.openAct(MainActivity.this, DataUtil.SAMPLE_URL);
                startActivity(new Intent(MainActivity.this, SettingAct.class));
            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                popupInputPswView.showCenter(findViewById(R.id.test_tv));
            }
        },300);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if(popupInputPswView!=null&&popupInputPswView.isShow()){
            return false;
        }
        return super.dispatchTouchEvent(ev);
    }
}