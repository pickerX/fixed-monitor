package com.fixed.monitor.model.popup;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.fixed.monitor.R;
import com.fixed.monitor.base.PopupBaseView;
import com.fixed.monitor.util.MeasureUtil;
import com.fixed.monitor.util.T;
import com.fixed.monitor.util.ToolUtil;
import com.fixed.monitor.util.VideoPathUtil;

public class PopupSetVideoTimeView extends PopupBaseView {

    private EditText time_et;
    private TextView cancle_tv, ok_tv;

    public PopupSetVideoTimeView(Context context) {
        super(context);
    }

    @Override
    public int getLayoutID() {
        return R.layout.popup_set_videorecord_time;
    }

    @Override
    public void bulidPopupWindow() {
//        super.bulidPopupWindow();
        this.popupWindow =
                new PopupWindow(popView, MeasureUtil.dip2px(context,270),
                        ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void initPopupView() {
        time_et = popView.findViewById(R.id.time_et);
        cancle_tv = popView.findViewById(R.id.cancle_tv);
        ok_tv = popView.findViewById(R.id.ok_tv);

        cancle_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        ok_tv.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                int time = ToolUtil.string2Int(time_et.getText().toString());
                if (time == 0) {
                    T.showShort(context, "请填写时间且不能为0");
                    return;
                }
                VideoPathUtil.saveVideoRecordTime(context, time);
                dismiss();
                ToolUtil.reStartApp(context);
            }
        });
    }
}
