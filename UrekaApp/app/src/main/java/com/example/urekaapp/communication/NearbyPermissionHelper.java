package com.example.urekaapp.communication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class NearbyPermissionHelper {

    private static final int REQUEST_CODE_NEARBY_PERMISSIONS = 102;

    private static final String[] ANDROID_12_NEARBY_PERMISSIONS = new String[]{
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_ADVERTISE
    };

    private static final String[] LOCATION_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    public static boolean hasPermissions(AppCompatActivity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+：检查 BLE_SCAN、BLE_CONNECT 和 BLE_ADVERTISE 权限
            for (String permission : ANDROID_12_NEARBY_PERMISSIONS) {
                if (ActivityCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        } else {
            // Android 11 及以下：检查定位权限
            for (String permission : LOCATION_PERMISSIONS) {
                if (ActivityCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    public static void requestPermissions(AppCompatActivity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(activity, ANDROID_12_NEARBY_PERMISSIONS, REQUEST_CODE_NEARBY_PERMISSIONS);
        } else {
            ActivityCompat.requestPermissions(activity, LOCATION_PERMISSIONS, REQUEST_CODE_NEARBY_PERMISSIONS);
        }
    }

    public static void handlePermissionResult(AppCompatActivity activity, int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_NEARBY_PERMISSIONS) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                Toast.makeText(activity, "Nearby permissions granted.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(activity, "Nearby permissions are required for Nearby operations.", Toast.LENGTH_LONG).show();
            }
        }
    }

    public static boolean ensurePermissions(AppCompatActivity activity) {
        if (!hasPermissions(activity)) {
            requestPermissions(activity);
            return false;
        }
        return true;
    }
}
