package com.fixed.monitor.model.log;

import android.content.Context;
import android.content.Intent;
import android.widget.TextView;

import com.fixed.monitor.R;
import com.fixed.monitor.base.BaseAct;
import com.fixed.monitor.util.ToolUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

public class LogDetailAct extends BaseAct {

    public static void openAct(Context context, String name, String path) {
        Intent intent = new Intent(context, LogDetailAct.class);
        intent.putExtra("name", name);
        intent.putExtra("path", path);
        context.startActivity(intent);
    }

    private TextView content_tv;

    @Override
    public int setLayoutID() {
        return R.layout.act_logdetail;
    }

    @Override
    public void initView() {
        setBackPress();
        content_tv = findViewById(R.id.content_tv);
    }

    @Override
    public void doBusiness() {
        String name = getIntent().getStringExtra("name");
        String path = getIntent().getStringExtra("path");
        if (ToolUtil.isNull(path)) {
            finish();
            return;
        }
        setTitleTx(name);
        content_tv.setText(getFileContent(path));
    }

    @Override
    public void doWeakUp() {

    }

    /**
     * 获取文件
     *
     * @return
     */
    public String getFileContent(String filePath) {
        String content = "";
        String path = filePath;
        File file = new File(path);
        try {
            InputStream inputStream = new FileInputStream(file);
            if (inputStream != null) {
                InputStreamReader reader = new InputStreamReader(inputStream, "GB2312");
                BufferedReader bufferedReader = new BufferedReader(reader);

                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    content += line + "\n";
                }
                inputStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return content;
    }
}
