package com.example.urekaapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.nearby.Nearby;
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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ureka.framework.logic.DeviceController;
import ureka.framework.model.data_model.ThisDevice;
import ureka.framework.model.message_model.UTicket;
import ureka.framework.resource.crypto.SerializationUtil;

public class VoterAgentActivity extends AppCompatActivity {
    private DeviceController deviceController;

    ConnectionsClient connectionsClient;
    private Set<String> connectedEndpoints = new HashSet<>();

    private Button buttonDiscover;
    private Button buttonRequestUTicket;
    private Button buttonApplyUTicket;
    private Button buttonShowRTicket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_voter_agent);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        deviceController = new DeviceController(ThisDevice.USER_AGENT_OR_CLOUD_SERVER, "User Agent");
        deviceController.getExecutor()._executeOneTimeInitializeAgentOrServer();

        connectionsClient = Nearby.getConnectionsClient(this);

        this.buttonDiscover = findViewById(R.id.buttonDiscover);
        this.buttonRequestUTicket = findViewById(R.id.buttonRequestUTicket);
        this.buttonRequestUTicket.setEnabled(false);
        this.buttonApplyUTicket = findViewById(R.id.buttonApplyUTicket);
        this.buttonApplyUTicket.setEnabled(false);
        this.buttonShowRTicket = findViewById(R.id.buttonShowRTicket);
        this.buttonShowRTicket.setEnabled(false);

        buttonDiscover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connectionsClient.startDiscovery(
                                getPackageName(),
                                endpointDiscoveryCallback,
                                new DiscoveryOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build()  // P2P
                        ).addOnSuccessListener(unused -> Log.d("Nearby", "Discovery started"))
                        .addOnFailureListener(e -> Log.d("Nearby", "Discovery failed", e));
            }
        });

        buttonRequestUTicket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO
                // TODO: fix the next line
                String targetDeviceId = ""; // = this.iotDevice.getSharedData().getThisDevice().getDevicePubKeyStr();
                String generatedTaskScope = SerializationUtil.dictToJsonStr(Map.of("ALL", "allow"));
                Map<String, String> generatedRequest = Map.of(
                        "deviceId", targetDeviceId,
                        "holderId", deviceController.getSharedData().getThisPerson().getPersonPubKeyStr(),
                        "uTicketType", UTicket.TYPE_ACCESS_UTICKET,
                        "taskScope", generatedTaskScope
                );
//                this.userAgentDO.getFlowIssuerIssueUTicket().issuerIssueUTicketToHolder(targetDeviceId,generatedRequest);
//                deviceController.getMsgReceiver()._recvXxxMessage();
            }
        });

        buttonApplyUTicket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO
                // TODO: fix the next line
                String targetDeviceId = ""; // = this.iotDevice.getSharedData().getThisDevice().getDevicePubKeyStr();
                String generatedCommand = "HELLO-1";
                deviceController.getFlowApplyUTicket().holderApplyUTicket(targetDeviceId,generatedCommand);
//                this.iotDevice.getMsgReceiver()._recvXxxMessage();
//                this.cloudServerEP.getMsgReceiver()._recvXxxMessage();
//                this.iotDevice.getMsgReceiver()._recvXxxMessage();
//                this.cloudServerEP.getMsgReceiver()._recvXxxMessage();
            }
        });

        buttonShowRTicket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO
            }
        });
    }

    EndpointDiscoveryCallback endpointDiscoveryCallback = new EndpointDiscoveryCallback() {
        @Override
        public void onEndpointFound(String endpointId, DiscoveredEndpointInfo info) {
            Log.d("Nearby", "Endpoint found: " + endpointId);
            if (!isConnectedToEndpoint(endpointId)) {
                connectionsClient.requestConnection(
                                "Voter Agent",
                                endpointId,
                                connectionLifecycleCallback
                        ).addOnSuccessListener(unused -> Log.d("Nearby", "Connection requested"))
                        .addOnFailureListener(e -> Log.d("Nearby", "Connection request failed: " + e.getMessage()));
            } else {
                Log.d("Nearby", "Already connected to endpoint: " + endpointId);

            }
        }

        @Override
        public void onEndpointLost(String endpointId) {
            Log.d("Nearby", "Endpoint lost: " + endpointId);
        }

        private boolean isConnectedToEndpoint(String endpointId) {
            return connectedEndpoints.contains(endpointId);
        }
    };

    ConnectionLifecycleCallback connectionLifecycleCallback = new ConnectionLifecycleCallback() {
        @Override
        public void onConnectionInitiated(String endpointId, ConnectionInfo info) {
            Log.d("Nearby", "Connection initiated with: " + endpointId);
            connectionsClient.acceptConnection(endpointId, payloadCallback);
        }

        @Override
        public void onConnectionResult(String endpointId, ConnectionResolution result) {
            if (result.getStatus().isSuccess()) {
                Log.d("Nearby", "Successfully connected to: " + endpointId);
                connectedEndpoints.add(endpointId);

                buttonRequestUTicket.setEnabled(true);
                buttonApplyUTicket.setEnabled(true);
                buttonShowRTicket.setEnabled(true);

            } else {
                Log.d("Nearby", "Connection failed with: " + endpointId);
            }
        }

        @Override
        public void onDisconnected(String endpointId) {
            Log.d("Nearby", "Disconnected from: " + endpointId);
            connectedEndpoints.remove(endpointId);
        }
    };

    PayloadCallback payloadCallback = new PayloadCallback() {
        @Override
        public void onPayloadReceived(String endpointId, Payload payload) {
            byte[] receivedBytes = payload.asBytes();
            if (receivedBytes != null) {
                String receivedJson = new String(receivedBytes);
                Log.d("Nearby", "Received JSON: " + receivedJson);

                // receivedJsonTextView.setText(receivedJson);
            }
        }

        @Override
        public void onPayloadTransferUpdate(String endpointId, PayloadTransferUpdate update) {
            // 處理傳輸更新（例如顯示傳輸進度）
        }
    };
}