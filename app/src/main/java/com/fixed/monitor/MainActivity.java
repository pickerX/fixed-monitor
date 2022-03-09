package com.fixed.monitor;

import static androidx.navigation.Navigation.findNavController;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentContainerView;
import androidx.navigation.NavController;

import com.fixed.monitor.service.MonitorService;
import com.fixed.monitor.util.ToolUtil;
import com.fixed.monitor.util.VideoPathUtil;
import com.zlylib.fileselectorlib.utils.Const;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    //    PopupInputPswView popupInputPswView;
    private FragmentContainerView mFragment;

//    private View psw_fl;
//    private MsCodeInputView mscode_ipv;
//    private TextView commit_tv;


    private View[] tab_ll = new View[3];
    private ImageView[] tab_iv = new ImageView[3];
    private TextView[] tab_tv = new TextView[3];

    public MonitorService monitorService;
    private ServiceConnection coreServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            monitorService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            monitorService = ((MonitorService.MonitorServiceBinder) service).getService();
            monitorService.bindLifecycleOwner(MainActivity.this);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bindService(MonitorService.getIntent(), coreServiceConnection, Context.BIND_AUTO_CREATE);
        mFragment = findViewById(R.id.fragment_container);

        tab_ll[0] = findViewById(R.id.tab1_ll);
        tab_ll[1] = findViewById(R.id.tab2_ll);
        tab_ll[2] = findViewById(R.id.tab3_ll);
        tab_iv[0] = findViewById(R.id.tab1_iv);
        tab_iv[1] = findViewById(R.id.tab2_iv);
        tab_iv[2] = findViewById(R.id.tab3_iv);
        tab_tv[0] = findViewById(R.id.tab1_tv);
        tab_tv[1] = findViewById(R.id.tab2_tv);
        tab_tv[2] = findViewById(R.id.tab3_tv);

        for (View v : tab_ll) {
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    reSetTabView();
                    view.setSelected(true);
                    try {
                        NavController navHostController = findNavController(MainActivity.this, R.id.fragment_container);
                        switch (view.getId()) {
                            case R.id.tab1_ll:
                                navHostController.navigate(R.id.camera_fragment);
                                break;
                            case R.id.tab2_ll:
                                navHostController.navigate(R.id.videolist_fragment);
//                                startActivity(new Intent(MainActivity.this, VideoListAct.class));
                                break;
                            case R.id.tab3_ll:
                                navHostController.navigate(R.id.setting_fragment);
                                break;
                        }
                    } catch (Exception e) {

                    }
                }
            });
        }
        tab_ll[0].performClick();

//        psw_fl = findViewById(R.id.psw_fl);
//        psw_fl.setVisibility(View.VISIBLE);
//        commit_tv = findViewById(R.id.commit_tv);
//        mscode_ipv = findViewById(R.id.mscode_ipv);
//        mscode_ipv.setOnMsCodeInterface(new MsCodeInputView.MsCodeInterface() {
//            @Override
//            public void inputFinish(String pas) {
//                commit_tv.setEnabled(true);
//            }
//
//            @Override
//            public void inputUnReady() {
//                commit_tv.setEnabled(false);
//            }
//        });
//        commit_tv.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if(PasswordUtil.getPsw(MainActivity.this).equals(mscode_ipv.getNowText())){
//                    mscode_ipv.clearText();
//                    psw_fl.setVisibility(View.GONE);
//                    //关闭软键盘
//                    InputMethodManager imm = (InputMethodManager) MainActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
//                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
//                }else{
//                    T.showShort(MainActivity.this,"密码错误");
//                }
//            }
//        });
    }

    public void reSetTabView() {
        for (View v : tab_ll) {
            v.setSelected(false);
        }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && data != null) {
            ArrayList<String> list = data.getStringArrayListExtra(Const.EXTRA_RESULT_SELECTION);
            if (list.isEmpty()) {
                return;
            }
            String dir = list.get(0);
            VideoPathUtil.savePath(this, dir);

            mFragment.postDelayed(() -> ToolUtil.reStartApp(this), 1200L);
        }
    }

    public void tabClick(int i){
        try{
            tab_ll[i].performClick();
        }catch (Exception e){
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (monitorService != null) {
            unbindService(coreServiceConnection);
        }
    }
}