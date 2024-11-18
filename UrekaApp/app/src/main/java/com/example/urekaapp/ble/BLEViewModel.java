package com.example.urekaapp.ble;

import android.content.Context;

import androidx.lifecycle.ViewModel;

public class BLEViewModel extends ViewModel {
    private BLEManager bleManager;

    public BLEManager getBLEManager(Context context) {
        if (bleManager == null) {
            bleManager = new BLEManager(context.getApplicationContext());
        }
        return bleManager;
    }
}

