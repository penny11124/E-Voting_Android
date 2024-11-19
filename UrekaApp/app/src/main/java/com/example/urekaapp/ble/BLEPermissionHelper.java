package com.example.urekaapp.ble;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class BLEPermissionHelper {

    private static final int REQUEST_CODE_BLUETOOTH_PERMISSIONS = 101;

    private static final String[] ANDROID_12_BLE_PERMISSIONS = new String[]{
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT
    };

    private static final String[] LOCATION_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    public static boolean hasPermissions(AppCompatActivity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+：检查 BLE_SCAN 和 BLE_CONNECT
            for (String permission : ANDROID_12_BLE_PERMISSIONS) {
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
            ActivityCompat.requestPermissions(activity, ANDROID_12_BLE_PERMISSIONS, REQUEST_CODE_BLUETOOTH_PERMISSIONS);
        } else {
            ActivityCompat.requestPermissions(activity, LOCATION_PERMISSIONS, REQUEST_CODE_BLUETOOTH_PERMISSIONS);
        }
    }

    public static void handlePermissionResult(AppCompatActivity activity, int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_BLUETOOTH_PERMISSIONS) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                Toast.makeText(activity, "Bluetooth permissions granted.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(activity, "Bluetooth permissions are required for BLE operations.", Toast.LENGTH_LONG).show();
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