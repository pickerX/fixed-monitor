package com.fixed.monitor.model.log;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fixed.monitor.R;
import com.fixed.monitor.base.BaseAct;
import com.fixed.monitor.base.CrashExpection;
import com.fixed.monitor.base.adapter.MCommAdapter;
import com.fixed.monitor.base.adapter.MCommVH;
import com.fixed.monitor.bean.LogBean;
import com.fixed.monitor.util.FileProviderUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class LogListAct extends BaseAct {

    RecyclerView rcv;
    MCommAdapter adapter;

    @Override
    public int setLayoutID() {
        return R.layout.act_loglist;
    }

    @Override
    public void initView() {
        setBackPress();
        setTitleTx("系统日志");
        rcv = findViewById(R.id.rcv);
        adapter = new MCommAdapter(this, new MCommVH.MCommVHInterface<LogBean>() {
            @Override
            public int setLayout() {
                return R.layout.view_loglist_item;
            }

            @Override
            public void bindData(Context context, MCommVH mCommVH, int position, LogBean o) {
                mCommVH.setText(R.id.name_tv,o.name);
                mCommVH.setText(R.id.createtime_tv,o.createTime);

                mCommVH.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        LogDetailAct.openAct(context,o.name,o.path);
//                        Intent intent = new Intent(Intent.ACTION_VIEW);
//                        intent.addCategory(Intent.CATEGORY_DEFAULT);
//                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                        Uri uri = FileProviderUtil.getUri(LogListAct.this,new File(o.path));
//                        intent.setDataAndType(uri,   "text/plain");
//                        startActivity(intent);
                    }
                });
            }
        });
        adapter.setShowEmptyView(true);
        rcv.setLayoutManager(new LinearLayoutManager(this));
        rcv.setAdapter(adapter);
    }

    @Override
    public void doBusiness() {
           adapter.setData(getData());
    }

    public List<LogBean> getData() {
        List<LogBean> logBeans  = new ArrayList<>();
        String folderPath = CrashExpection.getInstance(this).getPath();
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
        return  logBeans;
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
