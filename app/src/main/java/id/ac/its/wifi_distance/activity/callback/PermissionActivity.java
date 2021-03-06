package id.ac.its.wifi_distance.activity.callback;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public abstract class PermissionActivity extends Activity {
    private final OnPermissionGrantedCallback callback;
    private final int REQUEST_CODE = 14045;
    private final String[] permissions;

    public PermissionActivity(String... permissions) {
        this.callback = OnPermissionGrantedCallback.class.cast(this);
        this.permissions = permissions;
    }

    @Override
    protected void onStart() {
        super.onStart();
        List<String> deniedPermissions = getDeniedPermissions(permissions);
        resolveDeniedPermissions(deniedPermissions);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE) {
            List<String> deniedPermissions = getDeniedPermissions(permissions, grantResults);
            resolveDeniedPermissions(deniedPermissions);
        }
    }

    private void resolveDeniedPermissions(List<String> deniedPermissions) {
        boolean isPermissionSafe = (deniedPermissions.isEmpty());
        if (isPermissionSafe) {
            callback.onPermissionGranted();
        } else {
            requestPermissionsAsync(deniedPermissions);
        }
    }

    private void requestPermissionsAsync(List<String> permissions) {
        if (!permissions.isEmpty()) {
            requestPermissions(
                    permissions.toArray(new String[permissions.size()]),
                    REQUEST_CODE
            );
        }
    }

    private List<String> getDeniedPermissions(String... permissions) {
        List<String> deniedPermissions = new ArrayList<>(permissions.length);
        for (String permission : permissions) {
            int grantResult = checkSelfPermission(permission);
            boolean isDenied = (grantResult == PackageManager.PERMISSION_DENIED);
            if (isDenied) {
                deniedPermissions.add(permission);
            }
        }
        return deniedPermissions;
    }

    private List<String> getDeniedPermissions(String[] permissions, int[] grantResults) {
        List<String> deniedPermissions = new ArrayList<>(grantResults.length);
        for (int i = 0; i < grantResults.length; i++) {
            int result = grantResults[i];
            boolean isDenied = (result == PackageManager.PERMISSION_DENIED);
            if (isDenied) {
                deniedPermissions.add(permissions[i]);
            }
        }
        return deniedPermissions;
    }
}
