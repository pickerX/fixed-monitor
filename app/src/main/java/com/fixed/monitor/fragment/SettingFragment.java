package com.fixed.monitor.fragment;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fixed.monitor.R;
import com.fixed.monitor.base.BaseFragment;
import com.fixed.monitor.base.CrashExpection;
import com.fixed.monitor.base.adapter.MCommAdapter;
import com.fixed.monitor.base.adapter.MCommVH;
import com.fixed.monitor.bean.LogBean;
import com.fixed.monitor.model.log.LogDetailAct;
import com.fixed.monitor.model.popup.PopupInputPswView;
import com.fixed.monitor.model.popup.PopupSetPswView;
import com.fixed.monitor.model.popup.PopupSetVideoTimeView;
import com.fixed.monitor.util.VideoPathUtil;
import com.zlylib.fileselectorlib.FileSelector;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SettingFragment extends BaseFragment {

    View[] views = new View[4];

    View setpath_fl, setDuringTime_rl, setPsw_rl;

    TextView savetime_tv;
    TextView dir_tv;

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
        dir_tv = view.findViewById(R.id.tv_dir);
        setDuringTime_rl = view.findViewById(R.id.setDuringTime_rl);
        setPsw_rl = view.findViewById(R.id.setPsw_rl);

        popupInputPswView = new PopupInputPswView(getContext(),
                new PopupInputPswView.PopupInputPswViewInterface() {
                    @Override
                    public void success() {
                        popupSetPswView.showCenter(setPsw_rl);
                    }

                    @Override
                    public void cancel() {
                        Navigation.findNavController(requireActivity(), R.id.fragment_container)
                                .navigate(R.id.camera_fragment);
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
                        LogDetailAct.openAct(getContext(), o.name, o.path);
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
                            loglist_rcv.setVisibility(View.VISIBLE);
//                            startActivity(new Intent(getContext(), LogListAct.class));
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
                popupSetVideoTimeView.showCenter(view);

            }
        });
        setPsw_rl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupInputPswView.showCenter(view);
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
        logAdapter.setData(getLogData());
        PopupInputPswView popupInputPswView = new PopupInputPswView(getContext(), new PopupInputPswView.PopupInputPswViewInterface() {
            @Override
            public void success() {

            }

            @Override
            public void cancel() {
                Navigation.findNavController(requireActivity(), R.id.fragment_container)
                        .navigate(R.id.camera_fragment);
            }
        });
        popupInputPswView.showCenter(setPsw_rl);
    }

    private void updateSaveDir() {
        String dir = VideoPathUtil.getPath(requireContext());
        if (TextUtils.isEmpty(dir)) return;

        dir = dir.replace("/storage/emulated/0", "/sdcard");
        dir_tv.setText(dir);
    }

    @Override
    public void onResume() {
        super.onResume();

        dir_tv.postDelayed(this::updateSaveDir, 300L);
    }

    public List<LogBean> getLogData() {
        List<LogBean> logBeans = new ArrayList<>();
        String folderPath = CrashExpection.getInstance(getContext()).getPath();
        File f = new File(folderPath);
        if (!f.exists()) {//判断路径是否存在
            return logBeans;
        }
        File[] files = f.listFiles();
        if (files == null) {//判断权限
            return logBeans;
        }
        for (File _file : files) {//遍历目录
            if (_file.isFile() && _file.getName().endsWith("txt")) {
                String _name = _file.getName();
                String filePath = _file.getAbsolutePath();//获取文件路径
//                String fileName = _file.getName().substring(0, _name.length() - 4);//获取文件名
                String fileName = _file.getName();//获取文件名
                String createTime = getFileLastModifiedTime(_file);

                LogBean logBean = new LogBean();
                logBean.name = _name;
                logBean.path = filePath;
                logBean.createTime = createTime;
                logBeans.add(logBean);
            }
        }

        Collections.sort(logBeans, new Comparator<LogBean>() {
            @Override
            public int compare(LogBean logBean, LogBean t1) {
                return t1.createTime.compareTo(logBean.createTime);
            }
        });
        return logBeans;
    }

    private static final String mformatType = "yyyy/MM/dd HH:mm:ss";

    public static String getFileLastModifiedTime(File file) {
        Calendar cal = Calendar.getInstance();
        long time = file.lastModified();
        SimpleDateFormat formatter = new SimpleDateFormat(mformatType);
        cal.setTimeInMillis(time);
        // 输出：修改时间[2] 2009-08-17 10:32:38
        return formatter.format(cal.getTime());
    }

    @Override
    public void doWeakUp() {

    }
}
