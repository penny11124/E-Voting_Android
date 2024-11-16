package com.example.urekaapp;

import android.content.Context;

import androidx.lifecycle.ViewModel;

import com.example.urekaapp.communication.BLEManager;

public class BLEViewModel extends ViewModel {
    private BLEManager bleManager;

    public BLEManager getBLEManager(Context context) {
        if (bleManager == null) {
            bleManager = new BLEManager(context.getApplicationContext());
        }
        return bleManager;
    }
}

