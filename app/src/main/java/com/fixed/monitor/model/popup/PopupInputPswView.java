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
import com.fixed.monitor.util.MeasureUtil;
import com.fixed.monitor.util.PasswordUtil;
import com.fixed.monitor.util.T;
import com.fixed.monitor.view.MsCodeInputView;

public class PopupInputPswView extends PopupBaseView {

    private MsCodeInputView mscode_ipv;
    private TextView commit_tv;
    private PopupInputPswViewInterface inputPswViewInterface;

    public interface PopupInputPswViewInterface{
        void success();
    }

    public PopupInputPswView(Context context,PopupInputPswViewInterface inputPswViewInterface) {
        super(context);
        this.inputPswViewInterface = inputPswViewInterface;
    }

    @Override
    public int getLayoutID() {
        return R.layout.popup_input_psw_view;
    }

    @SuppressLint("WrongConstant")
    @Override
    public void bulidPopupWindow() {
//        super.bulidPopupWindow();
        this.popupWindow =
                new PopupWindow(popView, ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
        this.popupWindow.setSoftInputMode(PopupWindow.INPUT_METHOD_NEEDED);
        this.popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }

    @Override
    public void initPopupView() {
        commit_tv = popView.findViewById(R.id.commit_tv);
        mscode_ipv = popView.findViewById(R.id.mscode_ipv);
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
                if(PasswordUtil.getPsw(context).equals(mscode_ipv.getNowText())){
                   dismiss();
                   mscode_ipv.clearText();
                   if(inputPswViewInterface!=null){
                       inputPswViewInterface.success();
                   }
                }else{
                    T.showShort(context,"密码错误");
                }
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
