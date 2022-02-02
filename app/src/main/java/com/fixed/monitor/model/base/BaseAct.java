package com.fixed.monitor.model.base;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.fixed.monitor.R;

public abstract class BaseAct extends AppCompatActivity {

    boolean isVisable;//Act是否可见，是否在前台

    /**
     * @param
     * @return
     * @description
     * @author jiejack
     * @time 2022/2/2 8:34 下午
     */
    public abstract int setLayoutID();


    /**
     * @param
     * @return
     * @description 处理view
     * @author jiejack
     * @time 2022/2/2 8:34 下午
     */
    public abstract void initView();


    /**
     * @param
     * @return
     * @description 业务逻辑
     * @author jiejack
     * @time 2022/2/2 8:34 下午
     */
    public abstract void doBusiness();

    /**
     * @param
     * @return
     * @description Act唤醒
     * @author jiejack
     * @time 2022/2/2 8:34 下午
     */
    public abstract void doWeakUp();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(setLayoutID());
        setBackPress();
        initView();
        doBusiness();
    }

    /**
     * @param
     * @return
     * @description
     * @author 设置返回
     * @time 2022/2/2 11:54 下午
     */
    public void setBackPress() {
        try {
            View v = findViewById(R.id.backView);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });
        } catch (Exception e) {

        }
    }

    /**
     * @param
     * @return
     * @description 
     * @author jiejack
     * @time 2022/2/2 11:55 下午
     */
    public void setTitleTx(String title){
        try {
            TextView textView = findViewById(R.id.title_tv);
            textView.setText(title);
        }catch (Exception e){
        }
    }

    /**
     * @param
     * @return
     * @description 
     * @author jiejack
     * @time 2022/2/2 11:55 下午
     */
    public void setTitleTx(int resid){
        try {
            TextView textView = findViewById(R.id.title_tv);
            textView.setText(getResources().getText(resid));
        }catch (Exception e){
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isVisable = true;
        doWeakUp();
    }

    @Override
    protected void onStop() {
        super.onStop();
        isVisable = true;
    }
}
