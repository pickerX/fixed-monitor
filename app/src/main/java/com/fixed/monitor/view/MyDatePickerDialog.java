package com.fixed.monitor.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;

import com.fixed.monitor.R;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

public class MyDatePickerDialog extends DatePickerDialog {

    public interface MyDatePickerDialogInterface {
        void doCancleClick();
    }

    private MyDatePickerDialogInterface myDatePickerDialogInterface;


    public void setMyDatePickerDialogInterface(MyDatePickerDialogInterface myDatePickerDialogInterface) {
        this.myDatePickerDialogInterface = myDatePickerDialogInterface;
    }

    public static MyDatePickerDialog newInstance(OnDateSetListener callBack, int year, int monthOfYear, int dayOfMonth) {
        MyDatePickerDialog ret = new MyDatePickerDialog();
        ret.initialize(callBack, year, monthOfYear, dayOfMonth);
        return ret;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        Button cancelButton = view.findViewById(R.id.mdtp_cancel);
        cancelButton.setOnClickListener(v -> {
            tryVibrate();
            if (getDialog() != null) getDialog().cancel();
            if (myDatePickerDialogInterface != null) {
                myDatePickerDialogInterface.doCancleClick();
            }
        });
        return view;
    }
}
