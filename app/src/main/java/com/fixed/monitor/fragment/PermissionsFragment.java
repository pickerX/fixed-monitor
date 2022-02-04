package com.fixed.monitor.fragment;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

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
