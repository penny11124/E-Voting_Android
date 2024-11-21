package com.example.urekaapp.communication;

import android.content.Context;
import androidx.lifecycle.ViewModel;

import ureka.framework.logic.stage_worker.MsgReceiver;

public class NearbyViewModel extends ViewModel {
    private NearbyManager nearbyManager;

    public NearbyManager getNearbyManager(Context context, MsgReceiver msgReceiver) {
        if (nearbyManager == null) {
            nearbyManager = new NearbyManager(context.getApplicationContext(), msgReceiver);
        }
        return nearbyManager;
    }
}
