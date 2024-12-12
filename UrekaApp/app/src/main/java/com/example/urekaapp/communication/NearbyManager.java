package com.example.urekaapp.communication;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;

import java.util.Objects;

import ureka.framework.Environment;
import ureka.framework.logic.stage_worker.MsgReceiver;
import ureka.framework.resource.crypto.SerializationUtil;

public class NearbyManager {
    private static final String TAG = "NearbyManager";
    private static final String SERVICE_ID = "com.example.urekaapp.communication";
    private ConnectionsClient connectionsClient;

    public void setMsgReceiver(MsgReceiver msgReceiver) {
        this.msgReceiver = msgReceiver;
    }

    private MsgReceiver msgReceiver;
    private NearbyViewModel nearbyViewModel = null;

    public NearbyManager(Context context, MsgReceiver msgReceiver) {
        this.connectionsClient = Nearby.getConnectionsClient(context);
        this.msgReceiver = msgReceiver;
    }

    public void setViewModel(NearbyViewModel nearbyViewModel) {
        this.nearbyViewModel = nearbyViewModel;
    }

    public void startAdvertising() {
        connectionsClient.startAdvertising(
                        "AdminAgent",
                        SERVICE_ID,  // Service ID
                        connectionLifecycleCallback,
                        new AdvertisingOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build()  // P2P
                ).addOnSuccessListener(unused -> Log.d("Nearby", "Advertising started"))
                .addOnFailureListener(e -> Log.d("Nearby", "Advertising failed" + e.getMessage(), e));
    }

    public void startDiscovery() {
        connectionsClient.startDiscovery(
                        SERVICE_ID,
                        endpointDiscoveryCallback,
                        new DiscoveryOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build()
                ).addOnSuccessListener(unused -> Log.d(TAG, "Discovery started"))
                .addOnFailureListener(e -> Log.d("Nearby", "Discovery failed", e));
    }

    public void stopAllActions() {
        connectionsClient.stopAdvertising();
        connectionsClient.stopDiscovery();
    }

    public void sendMessage(String endpointId, String message) {
        Payload payload = Payload.fromBytes(SerializationUtil.strToBytes(message));
        connectionsClient.sendPayload(endpointId, payload)
                .addOnSuccessListener(unused -> Log.d(TAG, "Payload sent successfully"))
                .addOnFailureListener(e -> Log.e(TAG, "Payload sending failed", e));
    }

    private final ConnectionLifecycleCallback connectionLifecycleCallback = new ConnectionLifecycleCallback() {
        @Override
        public void onConnectionInitiated(String endpointId, ConnectionInfo connectionInfo) {
            // Automatically accept the connection on connection initiation
            connectionsClient.acceptConnection(endpointId, payloadCallback);
            Log.d(TAG, "Connection initiated with " + endpointId);
        }

        @Override
        public void onConnectionResult(String endpointId, ConnectionResolution result) {
            if (result.getStatus().isSuccess()) {
                Log.d(TAG, "Connected to " + endpointId);
                Environment.connectedEndpointId = endpointId;
                if (nearbyViewModel != null) {
                    nearbyViewModel.setConnected(true);
                }
            } else {
                Log.e(TAG, "Connection failed to " + endpointId);
            }
        }

        @Override
        public void onDisconnected(String endpointId) {
            Log.d(TAG, "Disconnected from " + endpointId);
            Environment.connectedEndpointId = null;
            if (nearbyViewModel != null) {
                nearbyViewModel.setConnected(false);
            }
        }
    };

     final EndpointDiscoveryCallback endpointDiscoveryCallback =
            new EndpointDiscoveryCallback() {
                @Override
                public void onEndpointFound(String endpointId, DiscoveredEndpointInfo info) {
                    Log.d(TAG, "Endpoint found: " + endpointId);
                    // Automatically request a connection to the found endpoint
                    if (!isConnectedToEndpoint(endpointId)) {
                        connectionsClient.requestConnection("VoterAgent", endpointId, connectionLifecycleCallback)
                                .addOnSuccessListener(unused -> Log.d(TAG, "Connection request sent"))
                                .addOnFailureListener(e -> Log.e(TAG, "Connection request failed", e));
                    } else {
                        Log.d("Nearby", "Already connected to endpoint: " + endpointId);
                    }
                }

                @Override
                public void onEndpointLost(String endpointId) {
                    Log.d(TAG, "Endpoint lost: " + endpointId);
                }

                private boolean isConnectedToEndpoint(String endpointId) {
                    return Objects.equals(Environment.connectedEndpointId, endpointId);
                }
            };

    private final PayloadCallback payloadCallback = new PayloadCallback() {
        @Override
        public void onPayloadReceived(String endpointId, Payload payload) {
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

    public void disconnectFromAllEndpoints() {
        if (Environment.connectedEndpointId != null) {
            connectionsClient.disconnectFromEndpoint(Environment.connectedEndpointId);
        } else {
            Log.d(TAG, "No active connections to disconnect");
        }
    }

}
