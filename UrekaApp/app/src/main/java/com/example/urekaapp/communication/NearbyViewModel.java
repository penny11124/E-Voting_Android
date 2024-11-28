package com.example.urekaapp.communication;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import ureka.framework.logic.stage_worker.MsgReceiver;

public class NearbyViewModel extends ViewModel {
    private NearbyManager nearbyManager;
    private final MutableLiveData<Boolean> isConnected = new MutableLiveData<>(false);

    public NearbyManager getNearbyManager(Context context, MsgReceiver msgReceiver) {
        if (nearbyManager == null) {
            nearbyManager = new NearbyManager(context.getApplicationContext(), msgReceiver);
        }
        return nearbyManager;
    }

    public LiveData<Boolean> getIsConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected.setValue(connected);
    }
}
