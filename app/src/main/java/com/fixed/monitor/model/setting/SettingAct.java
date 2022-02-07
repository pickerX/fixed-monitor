package com.fixed.monitor.model.setting;

import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.fixed.monitor.R;
import com.fixed.monitor.base.BaseAct;
import com.fixed.monitor.model.log.LogListAct;
import com.fixed.monitor.model.popup.PopupInputPswView;
import com.fixed.monitor.model.popup.PopupSetPswView;
import com.fixed.monitor.model.popup.PopupSetVideoTimeView;
import com.fixed.monitor.util.VideoPathUtil;
import com.zlylib.fileselectorlib.FileSelector;
import com.zlylib.fileselectorlib.utils.Const;

import java.util.ArrayList;

public class SettingAct extends BaseAct {

    View setpath_fl, setpsw_fl, loadcat_rl, savetime_fl;
    TextView path_tv;
    TextView savetime_tv;
    PopupInputPswView popupInputPswView;
    PopupSetPswView popupSetPswView;
    PopupSetVideoTimeView popupSetVideoTimeView;

    @Override
    public int setLayoutID() {
        return R.layout.act_setting;
    }

    @Override
    public void initView() {
        setTitleTx("设置");
        path_tv = findViewById(R.id.path_tv);
        savetime_tv = findViewById(R.id.savetime_tv);

        setpath_fl = findViewById(R.id.setpath_fl);
        setpath_fl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FileSelector.from(SettingAct.this)
                        .onlyShowFolder()  //只能选择文件夹
                        .requestCode(1) //设置返回码
                        .start();
            }
        });
        popupSetPswView = new PopupSetPswView(SettingAct.this);
        popupInputPswView = new PopupInputPswView(SettingAct.this,
                new PopupInputPswView.PopupInputPswViewInterface() {
                    @Override
                    public void success() {
                        popupSetPswView.showCenter(setpsw_fl);
                    }
                });

        setpsw_fl = findViewById(R.id.setpsw_fl);
        setpsw_fl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupInputPswView.showCenter(setpsw_fl);
            }
        });
        loadcat_rl = findViewById(R.id.loadcat_rl);
        loadcat_rl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SettingAct.this, LogListAct.class));
            }
        });

        popupSetVideoTimeView = new PopupSetVideoTimeView(this);
        savetime_tv.setText(VideoPathUtil.getVideoRecordTime(SettingAct.this) + "分钟");
        savetime_fl = findViewById(R.id.savetime_fl);
        savetime_fl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupSetVideoTimeView.showCenter(view);
            }
        });

    }

    @Override
    public void doBusiness() {
        path_tv.setText(VideoPathUtil.getPath(SettingAct.this));
    }

    @Override
    public void doWeakUp() {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (data != null) {
                ArrayList<String> essFileList = data.getStringArrayListExtra(Const.EXTRA_RESULT_SELECTION);
                if (essFileList.size() > 0) {
                    String path = essFileList.get(0);
                    VideoPathUtil.savePath(SettingAct.this, path);
                    path_tv.setText(path);
                }
            }
        }
    }
}
