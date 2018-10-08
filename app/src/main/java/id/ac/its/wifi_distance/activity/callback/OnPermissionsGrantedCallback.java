package id.ac.its.wifi_distance.activity.callback;

import android.app.Activity;
import android.content.pm.PackageManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class OnPermissionsGrantedCallback extends Activity {
    private int REQUEST_CODE = new Random().nextInt();

    protected void onRequestPermissions(String... permissions) {
        List<String> deniedPermissions = getDeniedPermissions(permissions);
        resolveDeniedPermissions(deniedPermissions);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_CODE) {
            List<String> deniedPermissions = new ArrayList<>(grantResults.length);
            for (int i = 0; i < grantResults.length; i++) {
                int result = grantResults[i];
                boolean isDenied = (result == PackageManager.PERMISSION_DENIED);
                if (isDenied) {
                    deniedPermissions.add(permissions[i]);
                }
            }
            resolveDeniedPermissions(deniedPermissions);
        }
    }

    private void resolveDeniedPermissions(List<String> deniedPermissions) {
        boolean isPermissionSafe = (deniedPermissions.isEmpty());
        if (isPermissionSafe) {
            onPermissionsGranted();
        } else {
            requestPermissions(deniedPermissions);
        }
    }

    private void requestPermissions(List<String> permissions) {
        if (!permissions.isEmpty()) {
            requestPermissions(
                    permissions.toArray(new String[permissions.size()]),
                    REQUEST_CODE
            );
        }
    }

    public abstract void onPermissionsGranted();
}
