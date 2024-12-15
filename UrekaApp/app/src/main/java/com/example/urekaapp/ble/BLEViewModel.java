package com.example.urekaapp.ble;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import ureka.framework.resource.logger.SimpleLogger;

public class BLEViewModel extends ViewModel {
    private BLEManager bleManager;
    private final MutableLiveData<Boolean> isConnected = new MutableLiveData<>(false);

    public BLEManager getBLEManager(Context context) {
        if (bleManager == null) {
            SimpleLogger.simpleLog("info", "BLEViewModel: bleManager is null");
            bleManager = new BLEManager(context.getApplicationContext());
        } else {
            SimpleLogger.simpleLog("info", "BLEViewModel: bleManager is not null");
        }
        return bleManager;
    }
}

