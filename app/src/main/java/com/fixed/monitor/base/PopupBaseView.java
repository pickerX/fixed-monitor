package com.fixed.monitor.base;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.PopupWindow;

import com.fixed.monitor.R;
import com.fixed.monitor.util.MeasureUtil;

public abstract class PopupBaseView {
    public int frameWidth, frameHeight;
    public int popupWidth, popupHeight;
    public Context context;
    public View popView;
    public PopupWindow popupWindow;


    public PopupBaseView(Context context) {
        this.context = context;
        this.frameWidth = MeasureUtil.getScreenWidth(context);
        this.frameHeight = MeasureUtil.getScreenHeight(context);
        this.popView = LayoutInflater.from(context).inflate(
                getLayoutID(), null);
        bulidPopupWindow();
        popupWindow.getContentView().measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        this.popupWidth = popupWindow.getContentView().getMeasuredWidth();
        this.popupHeight = popupWindow.getContentView().getMeasuredHeight();
        this.popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        // 设置popwindow出现和消失动画
//        this.popupWindow .setAnimationStyle(R.style.PopMenuAnimation2);
//        popupWindow.setBackgroundDrawable(BitmapDrawable.createFromPath(null));
        this.popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                backgroundAlpha(1f);
                onPopupDismiss();
            }
        });
        if (!isOutSideTouchClose()) {
            this.popupWindow.setFocusable(false);
            this.popupWindow.setOutsideTouchable(false);
            this.popupWindow.setTouchInterceptor(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                        return true;
                    }
                    return false;
                }
            });
        }else{
            this.popupWindow.setFocusable(true);
            this.popupWindow.setOutsideTouchable(true);
            this.popupWindow.setTouchInterceptor(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                        dismiss();
                        return true;
                    }
                    return false;
                }
            });
        }
        initPopupView();
    }

    public abstract int getLayoutID();

    public void bulidPopupWindow() {
        this.popupWindow = new PopupWindow(popView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    public abstract void initPopupView();


//    /**
//     * 显示popWindow
//     */
//    public void showPop(View parent) {
//        // 获取弹框的真实高度
//        popupWindow.getContentView().measure(View.MeasureSpec.UNSPECIFIED,
//                View.MeasureSpec.UNSPECIFIED);
//        // 获取popwindow焦点
//        popupWindow.setFocusable(true);
//        popupWindow.setTouchable(true);
//        popupWindow.setOutsideTouchable(false);
//    }


    /**
     * 底部弹框s
     *
     * @param parent
     */
    public void showCenter(View parent) {
        /* 获取当前系统的android版本号 */
        int currentapiVersion = Build.VERSION.SDK_INT;
        popupWindow.setAnimationStyle(R.style.ScaleAnimStyle);
        if (currentapiVersion < 24) {
            popupWindow.showAtLocation(parent, Gravity.CENTER, 0, 0);
            popupWindow.update();
        } else {
            popupWindow.dismiss();
            popupWindow.showAtLocation(parent, Gravity.CENTER, 0, 0);
        }
        backgroundAlpha(0.5f);
    }

    /**
     * 底部弹框
     *
     * @param parent
     */
    public void showBottom(View parent) {
        /* 获取当前系统的android版本号 */
        int currentapiVersion = Build.VERSION.SDK_INT;
        popupWindow.setAnimationStyle(R.style.BottomAnimStyle);
        if (currentapiVersion < 24) {
            popupWindow.showAtLocation(parent, Gravity.BOTTOM, 0, 0);
            popupWindow.update();
        } else {
            popupWindow.dismiss();
            popupWindow.showAtLocation(parent, Gravity.BOTTOM, 0, 0);
        }
        backgroundAlpha(0.5f);
    }


    /**
     * 底部弹框s
     *
     * @param parent
     */
    public void showAsDropDown(View parent, int x, int y) {
        /* 获取当前系统的android版本号 */
        int currentapiVersion = Build.VERSION.SDK_INT;
        popupWindow.setAnimationStyle(R.style.TopAnimStyle);
        if (currentapiVersion < 24) {
//            popupWindow.showAtLocation(parent, Gravity.CENTER, x, y);
            popupWindow.showAsDropDown(parent,x,y);
            popupWindow.update();
        } else {
            popupWindow.dismiss();
            popupWindow.showAsDropDown(parent,x,y);
        }
        backgroundAlpha(0.5f);
    }


    /**
     * 设置添加屏幕的背景透明度
     *
     * @param bgAlpha
     */
    public void backgroundAlpha(float bgAlpha) {
        if (!needBgAlpha()) {
            return;
        }
        WindowManager.LayoutParams lp = ((Activity) context).getWindow()
                .getAttributes();
        lp.alpha = bgAlpha; // 0.0-1.0
        ((Activity) context).getWindow().setAttributes(lp);
    }

    public void dismiss() {
        if (popupWindow != null) {
            popupWindow.dismiss();
        }
    }

    public void onPopupDismiss(){

    }

    public boolean isShow() {
        return popupWindow == null ? false : popupWindow.isShowing();
    }

    public boolean needBgAlpha() {
        return true;
    }

    public boolean isOutSideTouchClose() {
        return true;
    }
}
