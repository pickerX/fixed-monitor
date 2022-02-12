package com.fixed.monitor.fragment;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fixed.monitor.R;
import com.fixed.monitor.base.BaseFragment;
import com.fixed.monitor.base.adapter.MCommAdapter;
import com.fixed.monitor.base.adapter.MCommVH;
import com.fixed.monitor.bean.LogBean;
import com.fixed.monitor.model.log.LogDetailAct;
import com.fixed.monitor.model.log.LogListAct;
import com.fixed.monitor.model.popup.PopupInputPswView;
import com.fixed.monitor.model.popup.PopupSetPswView;
import com.fixed.monitor.model.popup.PopupSetVideoTimeView;
import com.fixed.monitor.model.setting.SettingAct;
import com.fixed.monitor.util.VideoPathUtil;
import com.zlylib.fileselectorlib.FileSelector;

public class SettingFragment extends BaseFragment {

    View[] views = new View[4];

    View setpath_fl, setDuringTime_rl, setPsw_rl;

    TextView savetime_tv;

    RecyclerView loglist_rcv;
    MCommAdapter logAdapter;

    PopupInputPswView popupInputPswView;
    PopupSetPswView popupSetPswView;
    PopupSetVideoTimeView popupSetVideoTimeView;

    @Override
    public int setLayoutID() {
        return R.layout.fragment_setting;
    }

    @Override
    public void initView(View view) {
        views[0] = view.findViewById(R.id.setting_ll1);
        views[1] = view.findViewById(R.id.setting_ll2);
        views[2] = view.findViewById(R.id.setting_ll3);
        views[3] = view.findViewById(R.id.setting_ll4);

        setpath_fl = view.findViewById(R.id.setpath_fl);
        setDuringTime_rl = view.findViewById(R.id.setpath_fl);
        setPsw_rl = view.findViewById(R.id.setpath_fl);

        popupInputPswView = new PopupInputPswView(getContext(),
                new PopupInputPswView.PopupInputPswViewInterface() {
                    @Override
                    public void success() {
                        popupSetPswView.showCenter(setPsw_rl);
                    }
                });
        popupSetPswView = new PopupSetPswView(getContext());
        popupSetVideoTimeView = new PopupSetVideoTimeView(getContext());

        savetime_tv = view.findViewById(R.id.savetime_tv);
        savetime_tv.setText(VideoPathUtil.getVideoRecordTime(getContext()) + "分钟");

        loglist_rcv = view.findViewById(R.id.loglist_rcv);
        logAdapter = new MCommAdapter(getContext(), new MCommVH.MCommVHInterface<LogBean>() {
            @Override
            public int setLayout() {
                return R.layout.view_loglist_item;
            }

            @Override
            public void bindData(Context context, MCommVH mCommVH, int position, LogBean o) {
                mCommVH.setText(R.id.name_tv, o.name);
                mCommVH.setText(R.id.createtime_tv, o.createTime);

                mCommVH.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                    }
                });
            }
        });
        logAdapter.setShowEmptyView(true);
        loglist_rcv.setLayoutManager(new LinearLayoutManager(getContext()));
        loglist_rcv.setAdapter(logAdapter);

        for (View v : views) {
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    reSetRlView();
                    view.setSelected(true);
                    switch (view.getId()) {
                        case R.id.setting_ll1://保存路径
                            setpath_fl.setVisibility(View.VISIBLE);
                            break;
                        case R.id.setting_ll2://循环录制时长
                            setDuringTime_rl.setVisibility(View.VISIBLE);
                            break;
                        case R.id.setting_ll3://设置密码
                            setPsw_rl.setVisibility(View.VISIBLE);
                            break;
                        case R.id.setting_ll4://系统日志
//                            loglist_rcv.setVisibility(View.VISIBLE);
                            startActivity(new Intent(getContext(), LogListAct.class));
                            break;
                    }
                }
            });
        }
        setpath_fl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FileSelector.from((AppCompatActivity) getContext())
                        .onlyShowFolder()  //只能选择文件夹
                        .requestCode(1) //设置返回码
                        .start();
            }
        });
        setDuringTime_rl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupInputPswView.showCenter(view);
            }
        });
        setPsw_rl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupSetVideoTimeView.showCenter(view);
            }
        });
        views[0].performClick();
    }

    public void reSetRlView() {
        for (View v : views) {
            v.setSelected(false);
        }
        setpath_fl.setVisibility(View.GONE);
        setDuringTime_rl.setVisibility(View.GONE);
        setPsw_rl.setVisibility(View.GONE);
        loglist_rcv.setVisibility(View.GONE);
    }

    @Override
    public void doBusiness() {

    }

    @Override
    public void doWeakUp() {

    }
}
