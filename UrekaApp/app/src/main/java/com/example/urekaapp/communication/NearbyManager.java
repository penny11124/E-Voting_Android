package com.example.urekaapp.communication;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;

import ureka.framework.Environment;
import ureka.framework.logic.stage_worker.MsgReceiver;

public class NearbyManager {
    private static final String TAG = "NearbyManager";
    private static final String SERVICE_ID = "com.example.urekaapp.SERVICE_ID";
    private final ConnectionsClient connectionsClient;
    private final Context context;
    private MsgReceiver msgReceiver;

    public NearbyManager(Context context) {
        this.context = context;
        this.connectionsClient = Nearby.getConnectionsClient(context);
    }

    public void setMsgReceiver(MsgReceiver msgReceiver) {
        this.msgReceiver = msgReceiver;
    }

    public void startAdvertising(String deviceName) {
        connectionsClient.startAdvertising(
                        deviceName,
                        SERVICE_ID,
                        connectionLifecycleCallback,
                        new com.google.android.gms.nearby.connection.AdvertisingOptions.Builder()
                                .setStrategy(Strategy.P2P_STAR)
                                .build()
                ).addOnSuccessListener(unused -> Log.d(TAG, "Advertising started"))
                .addOnFailureListener(e -> Log.e(TAG, "Advertising failed", e));
    }

    public void startDiscovery(String deviceName) {
        connectionsClient.startDiscovery(
                        SERVICE_ID,
                        endpointDiscoveryCallback,
                        new com.google.android.gms.nearby.connection.DiscoveryOptions.Builder()
                                .setStrategy(Strategy.P2P_STAR)
                                .build()
                ).addOnSuccessListener(unused -> Log.d(TAG, "Discovery started"))
                .addOnFailureListener(e -> Log.e(TAG, "Discovery failed", e));
    }

    public void stopAllActions() {
        connectionsClient.stopAdvertising();
        connectionsClient.stopDiscovery();
    }

    public void sendMessage(String endpointId, String message) {
        Payload payload = Payload.fromBytes(message.getBytes());
        connectionsClient.sendPayload(endpointId, payload)
                .addOnSuccessListener(unused -> Log.d(TAG, "Payload sent successfully"))
                .addOnFailureListener(e -> Log.e(TAG, "Payload sending failed", e));
    }

    private final ConnectionLifecycleCallback connectionLifecycleCallback = new ConnectionLifecycleCallback() {
        @Override
        public void onConnectionInitiated(@NonNull String endpointId, @NonNull ConnectionInfo connectionInfo) {
            // Automatically accept the connection on connection initiation
            connectionsClient.acceptConnection(endpointId, payloadCallback);
            Log.d(TAG, "Connection initiated with " + endpointId);
        }

        @Override
        public void onConnectionResult(@NonNull String endpointId, @NonNull ConnectionResolution result) {
            if (result.getStatus().isSuccess()) {
                Log.d(TAG, "Connected to " + endpointId);
                Environment.connectedEndpointId = endpointId;
            } else {
                Log.e(TAG, "Connection failed to " + endpointId);
            }
        }

        @Override
        public void onDisconnected(@NonNull String endpointId) {
            Log.d(TAG, "Disconnected from " + endpointId);
            Environment.connectedEndpointId = null;
        }
    };

    private final EndpointDiscoveryCallback endpointDiscoveryCallback =
            new EndpointDiscoveryCallback() {
                @Override
                public void onEndpointFound(@NonNull String endpointId, @NonNull DiscoveredEndpointInfo info) {
                    Log.d(TAG, "Endpoint found: " + endpointId);
                    // Automatically request a connection to the found endpoint
                    connectionsClient.requestConnection("NearbyManager", endpointId, connectionLifecycleCallback)
                            .addOnSuccessListener(unused -> Log.d(TAG, "Connection request sent"))
                            .addOnFailureListener(e -> Log.e(TAG, "Connection request failed", e));
                }

                @Override
                public void onEndpointLost(@NonNull String endpointId) {
                    Log.d(TAG, "Endpoint lost: " + endpointId);
                }
            };

    private final PayloadCallback payloadCallback = new PayloadCallback() {
        @Override
        public void onPayloadReceived(@NonNull String endpointId, @NonNull Payload payload) {
            String message = new String(payload.asBytes());
            Log.d(TAG, "Payload received from " + endpointId + ": " + message);
            if (msgReceiver != null) {
                msgReceiver._recvXxxMessage(message);
            } else {
                Log.e(TAG, "MsgReceiver is not set");
            }
        }

        @Override
        public void onPayloadTransferUpdate(@NonNull String endpointId, @NonNull PayloadTransferUpdate update) {
            Log.d(TAG, "Payload transfer update from " + endpointId);
        }
    };
}
