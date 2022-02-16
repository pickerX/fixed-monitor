package com.fixed.monitor.model.popup;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.fixed.monitor.R;
import com.fixed.monitor.base.PopupBaseView;
import com.fixed.monitor.util.PasswordUtil;
import com.fixed.monitor.util.T;
import com.fixed.monitor.view.MsCodeInputView;

public class PopupSetPswView extends PopupBaseView {

    private MsCodeInputView mscode_ipv;
    private MsCodeInputView mscode_ipv2;
    private TextView commit_tv,cancle_tv;

    public PopupSetPswView(Context context) {
        super(context);
    }

    @Override
    public int getLayoutID() {
        return R.layout.popup_set_psw_view;
    }

    @SuppressLint("WrongConstant")
    @Override
    public void bulidPopupWindow() {
//        super.bulidPopupWindow();
        this.popupWindow =
                new PopupWindow(popView, ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
//        this.popupWindow.setSoftInputMode(PopupWindow.INPUT_METHOD_NEEDED);
//        this.popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }

    @Override
    public void initPopupView() {

        commit_tv = popView.findViewById(R.id.commit_tv);
        cancle_tv = popView.findViewById(R.id.cancle_tv);
        mscode_ipv = popView.findViewById(R.id.mscode_ipv);
        mscode_ipv2 = popView.findViewById(R.id.mscode_ipv2);
        mscode_ipv.setOnMsCodeInterface(new MsCodeInputView.MsCodeInterface() {
            @Override
            public void inputFinish(String pas) {
                if(mscode_ipv2.checkIsAllInput()) {
                    commit_tv.setEnabled(true);
                }
            }

            @Override
            public void inputUnReady() {
                commit_tv.setEnabled(false);
            }
        });
        mscode_ipv2.setOnMsCodeInterface(new MsCodeInputView.MsCodeInterface() {
            @Override
            public void inputFinish(String pas) {
                if(mscode_ipv.checkIsAllInput()) {
                    commit_tv.setEnabled(true);
                }
            }

            @Override
            public void inputUnReady() {
                commit_tv.setEnabled(false);
            }
        });
        commit_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mscode_ipv.getNowText().equals(mscode_ipv2.getNowText())){
                    PasswordUtil.savePsw(context,mscode_ipv.getNowText());
                    T.showShort(context,"密码设置成功");
                    mscode_ipv.clearText();
                    mscode_ipv2.clearText();
                    dismiss();
                }else{
                    T.showShort(context,"两次密码不一致");
                }
            }
        });
        cancle_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               dismiss();
            }
        });
    }

    @Override
    public boolean needBgAlpha() {
//        return super.needBgAlpha();
    return false;
    }

    @Override
    public boolean isOutSideTouchClose() {
        return super.isOutSideTouchClose();
//    return false;
    }
}
