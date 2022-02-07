package com.fixed.monitor;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentContainerView;

import com.fixed.monitor.model.popup.PopupInputPswView;
import com.fixed.monitor.model.setting.SettingAct;
import com.fixed.monitor.model.video.VideoListAct;
import com.fixed.monitor.util.PasswordUtil;
import com.fixed.monitor.util.T;
import com.fixed.monitor.view.MsCodeInputView;

public class MainActivity extends AppCompatActivity {

    //    PopupInputPswView popupInputPswView;
    private FragmentContainerView mFragment;

    private View psw_fl;
    private MsCodeInputView mscode_ipv;
    private TextView commit_tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mFragment = findViewById(R.id.fragment_container);

        psw_fl = findViewById(R.id.psw_fl);
        psw_fl.setVisibility(View.VISIBLE);
        commit_tv = findViewById(R.id.commit_tv);
        mscode_ipv = findViewById(R.id.mscode_ipv);
        mscode_ipv.setOnMsCodeInterface(new MsCodeInputView.MsCodeInterface() {
            @Override
            public void inputFinish(String pas) {
                commit_tv.setEnabled(true);
            }

            @Override
            public void inputUnReady() {
                commit_tv.setEnabled(false);
            }
        });
        commit_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(PasswordUtil.getPsw(MainActivity.this).equals(mscode_ipv.getNowText())){
                    mscode_ipv.clearText();
                    psw_fl.setVisibility(View.GONE);
                }else{
                    T.showShort(MainActivity.this,"密码错误");
                }
            }
        });
    }


    @Override
    public void onBackPressed() {
//        super.onBackPressed();
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