package com.fixed.monitor.view;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.fixed.monitor.R;
import com.fixed.monitor.util.MeasureUtil;
import com.fixed.monitor.util.ToolUtil;

import java.util.ArrayList;
import java.util.List;

public class MsCodeInputView extends LinearLayout {
    private int textNum = 6;//输入框数量
    private int nowPos = -1;
    private int mWidth;
    private List<EditText> editTextViews = new ArrayList<>();
    private MsCodeInterface onMsCodeInterface;


    public interface MsCodeInterface {
        void inputFinish(String pas);

        void inputUnReady();
    }

    public MsCodeInputView(Context context) {
        this(context, null);
    }

    public MsCodeInputView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MsCodeInputView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MsCodeInputView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = getMeasuredWidth();
        int mHeigth = MeasureUtil.dip2px(getContext(), 50);
        setMeasuredDimension(mWidth, mHeigth);
    }

    public void initView() {
        this.setOrientation(LinearLayout.HORIZONTAL);
        this.setBackground(getContext().getResources().getDrawable(R.drawable.shape_ffb1b1b1_r4));
        initEditView();
    }


    public void initEditView() {
        this.removeAllViews();
        this.editTextViews.clear();
//        int wlsize = MeasureManager.dip2px(getContext(), 50);

        int jgsize = MeasureUtil.dip2px(getContext(), 1);
//        int wlsize = (mWidth - jgsize * textNum - 1) / textNum;
        for (int i = 0; i < textNum; i++) {
            EditText editText = new EditText(getContext());
            editText.setBackground(null);
            editText.setTag(i + "");
            editText.setTextColor(0xff333333);
            editText.setGravity(Gravity.CENTER);
            editText.setTextAlignment(TEXT_ALIGNMENT_CENTER);
            editText.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            editText.addTextChangedListener(new MyTextWatch(editText));
//            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
            editText.setInputType( InputType.TYPE_NUMBER_VARIATION_PASSWORD|InputType.TYPE_CLASS_NUMBER );
            editText.setOnKeyListener(new OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    //You can identify which key pressed buy checking keyCode value with KeyEvent.KEYCODE_
                    if (keyCode == KeyEvent.KEYCODE_DEL) {
                        //this is for backspace
                        if (TextUtils.isEmpty(((EditText) (v)).getText())) {
                            int nexPos = ToolUtil.string2Int(((EditText) (v)).getTag().toString()) - 1;
                            if (nexPos >= 0) {
//                                editTextViews.get(nexPos).setText("");
                                editTextViews.get(nexPos).requestFocus();
                            }
                            return true;
                        }
                    }
                    return false;
                }
            });
            editText.setOnFocusChangeListener(new OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        ((EditText) (v)).setSelection(((EditText) (v)).length());
                    }
                }
            });
            LayoutParams lp = new LayoutParams(0, LayoutParams.MATCH_PARENT);
            lp.weight = 1;
            this.addView(editText, lp);
            editTextViews.add(editText);
            if (i != textNum - 1) {
                View v = new View(getContext());
                LayoutParams vlp = new LayoutParams(jgsize, LayoutParams.MATCH_PARENT);
                v.setBackgroundColor(0xffe7e7e7);
                this.addView(v, vlp);
            }
        }
    }

    /**
     * @param
     * @return
     * @description 获取当前输入结果
     * @author jieja
     * @time 2021/11/20 10:21
     */
    public String getNowText() {
        String result = "";
        for (int i = 0; i < editTextViews.size(); i++) {
            result += editTextViews.get(i).getText().toString();
        }
        return result;
    }

    /**
     * @param
     * @return
     * @description 输入框监听
     * @author jieja
     * @time 2021/11/22 8:54
     */
    public void setOnMsCodeInterface(MsCodeInterface onMsCodeInterface) {
        this.onMsCodeInterface = onMsCodeInterface;
    }

    /**
     * @param
     * @return
     * @description 检查是否输入完毕
     * @author jieja
     * @time 2021/11/22 9:00
     */
    public boolean checkIsAllInput() {
        boolean isFlag = true;
        for (int i = 0; i < editTextViews.size(); i++) {
            if (TextUtils.isEmpty(editTextViews.get(i).getText())) {
                isFlag = false;
                break;
            }
        }
        return isFlag;
    }

    /**
     * @param
     * @return
     * @description 清空密码
     * @author jiejack
     * @time 2022/2/4 4:19 下午
     */
    public void clearText() {
        for (int i = 0; i < editTextViews.size(); i++) {
            editTextViews.get(i).setText("");
        }
    }

/**
 * @param
 * @author jieja
 * @description 输入监听
 * @return
 * @time 2021/11/22 8:54
 */
public class MyTextWatch implements TextWatcher {
    public EditText et = null;

    public MyTextWatch(EditText et) {
        this.et = et;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (s.length() > 1) {
            et.setText(s.charAt(s.length() - 1) + "");
            et.setSelection(et.length());
        } else {
            if (s.length() > 0) {
                int nexPos = ToolUtil.string2Int(et.getTag().toString()) + 1;
                if (nexPos < editTextViews.size()) {
                    editTextViews.get(nexPos).requestFocus();
                    et.setSelection(et.length());
//                    editTextViews.get(nexPos).setSelection(editTextViews.get(nexPos).length());
//                    if (onMsCodeInterface != null) {
//                        onMsCodeInterface.inputUnReady();
//                    }
                }
            }
            if (onMsCodeInterface != null) {
                if (checkIsAllInput()) {
                    onMsCodeInterface.inputFinish(getNowText());
                } else {
                    onMsCodeInterface.inputUnReady();
                }
            }
//                else {//全部输入完成
//                    if (onMsCodeInterface != null) {
//                        onMsCodeInterface.inputFinish(getNowText());
//                    }
//                }
        }
    }
}


}
