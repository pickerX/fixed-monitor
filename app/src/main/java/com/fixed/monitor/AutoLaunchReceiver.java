package com.fixed.monitor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.lib.record.Config;
import com.lib.record.ConfigBuilder;
import com.lib.record.Monitor;
import com.lib.record.MonitorFactory;

/**
 * @author pickerx
 * @date 2022/1/28 10:48 上午
 */
public class AutoLaunchReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) {
            return;
        }

        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Intent preview = new Intent(context, MainActivity.class);
            context.startActivity(preview);
        }
    }
}
