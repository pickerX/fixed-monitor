package com.fixed.monitor.fragment;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.fixed.monitor.AutoLaunchReceiver;
import com.fixed.monitor.R;

import java.util.Set;

/**
 * permission check
 *
 * @author pickerx
 * @date 2022/2/4 9:25 上午
 */
public class PermissionsFragment extends Fragment {

    private final String[] PERMISSIONS_REQUIRED = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // request auto launch when boot
//        if (!hasAutoLaunchPermission()) {
//            Intent intent = getAutostartSettingIntent(requireContext());
//            startActivity(intent);
//            return;
//        }
        // Request camera-related permissions
        ActivityResultContracts.RequestMultiplePermissions contract =
                new ActivityResultContracts.RequestMultiplePermissions();
        contract.createIntent(requireContext(), PERMISSIONS_REQUIRED);
        ActivityResultLauncher<String[]> session = registerForActivityResult(contract, result -> {
            boolean grantResults = true;
            Set<String> keys = result.keySet();
            for (String key : keys) {
                if (result.get(key) != null && !result.get(key)) {
                    grantResults = false;
                }
                Log.d("Permission", key + " > " + result.get(key));
            }
            if (grantResults) {
                Navigation.findNavController(requireActivity(), R.id.fragment_container).navigate(
                        PermissionsFragmentDirections.actionPermissionsToXcamera());
            } else {
                Toast.makeText(requireContext(),
                        "Permission request denied", Toast.LENGTH_LONG)
                        .show();
            }
        });
        session.launch(PERMISSIONS_REQUIRED);
    }

    public boolean hasAutoLaunchPermission() {
        ComponentName localComponentName = new ComponentName(requireContext(),
                AutoLaunchReceiver.class);
        int rt = requireContext().getPackageManager()
                .getComponentEnabledSetting(localComponentName);
        return rt == PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
    }

    /**
     * 获取自启动管理页面的Intent
     *
     * @param context context
     * @return 返回自启动管理页面的Intent
     */
    public static Intent getAutostartSettingIntent(Context context) {
        ComponentName componentName = null;
        String brand = Build.MANUFACTURER;
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        switch (brand.toLowerCase()) {
            case "samsung"://三星
                componentName = new ComponentName("com.samsung.android.sm", "com.samsung.android.sm.app.dashboard.SmartManagerDashBoardActivity");
                break;
            case "huawei"://华为
                //荣耀V8，EMUI 8.0.0，Android 8.0上，以下两者效果一样
                componentName = new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity");
                // componentName = new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity");//目前看是通用的
                break;
            case "xiaomi"://小米
                componentName = new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity");
                break;
            case "vivo"://VIVO
                // componentName = new ComponentName("com.iqoo.secure", "com.iqoo.secure.safaguard.PurviewTabActivity");
                componentName = new ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity");
                break;
            case "oppo"://OPPO
                // componentName = new ComponentName("com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity");
                componentName = new ComponentName("com.coloros.oppoguardelf", "com.coloros.powermanager.fuelgaue.PowerUsageModelActivity");
                break;
            case "yulong":
            case "360"://360
                componentName = new ComponentName("com.yulong.android.coolsafe", "com.yulong.android.coolsafe.ui.activity.autorun.AutoRunListActivity");
                break;
            case "meizu"://魅族
                componentName = new ComponentName("com.meizu.safe", "com.meizu.safe.permission.SmartBGActivity");
                break;
            case "oneplus"://一加
                componentName = new ComponentName("com.oneplus.security", "com.oneplus.security.chainlaunch.view.ChainLaunchAppListActivity");
                break;
            case "letv"://乐视
                intent.setAction("com.letv.android.permissionautoboot");
            default://其他
                intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                intent.setData(Uri.fromParts("package", context.getPackageName(), null));
                break;
        }
        intent.setComponent(componentName);
        return intent;
    }

    /**
     * Convenience method used to check if all permissions required by this app are granted
     */
    private boolean hasPermissions(Context context) {
        boolean granted = true;
        for (String it : PERMISSIONS_REQUIRED) {
            granted &= ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED;
        }
        return granted;
    }

}
