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
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;

import java.security.KeyPair;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ureka.framework.logic.DeviceController;
import ureka.framework.logic.pipeline_flow.FlowIssueUTicket;
import ureka.framework.model.data_model.ThisDevice;
import ureka.framework.model.message_model.RTicket;
import ureka.framework.model.message_model.UTicket;
import ureka.framework.resource.crypto.ECC;
import ureka.framework.resource.crypto.SerializationUtil;

public class AdminAgentActivity extends AppCompatActivity {
    private Map<String, String> dataFromBlockchain; // Assume we only have 1 vote
    private DeviceController deviceController;
//    private KeyStore keyStore;

    ConnectionsClient connectionsClient;
    private Set<String> connectedEndpoints = new HashSet<>();

    private Button buttonAdvertise;
    private Button buttonInit;
    private Button buttonGetData;
    private Button buttonApplyInitUTicket;
    private Button buttonApplyTallyUTicket;
    private Button buttonShowRTickets;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_agent);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // private fields initialization
        deviceController = new DeviceController(ThisDevice.USER_AGENT_OR_CLOUD_SERVER, "Admin Agent");
        deviceController.getExecutor()._executeOneTimeInitializeAgentOrServer();

        connectionsClient = Nearby.getConnectionsClient(this);

        // components initialization
        this.buttonAdvertise = findViewById(R.id.buttonAdvertise);
        this.buttonInit = findViewById(R.id.buttonInit);
        this.buttonInit.setEnabled(false);
        this.buttonGetData = findViewById(R.id.buttonGetData);
        this.buttonGetData.setEnabled(false);
        this.buttonApplyInitUTicket = findViewById(R.id.buttonApplyInitUTicket);
        this.buttonApplyInitUTicket.setEnabled(false);
        this.buttonApplyTallyUTicket = findViewById(R.id.buttonApplyTallyUTicket);
        this.buttonApplyTallyUTicket.setEnabled(false);
        this.buttonShowRTickets = findViewById(R.id.buttonShowRTickets);
        this.buttonShowRTickets.setEnabled(false);

        buttonAdvertise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connectionsClient.startAdvertising(
                                "Admin Agent",
                                getPackageName(),  // Service ID
                                connectionLifecycleCallback,
                                new AdvertisingOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build()  // P2P
                        ).addOnSuccessListener(unused -> Log.d("Nearby", "Advertising started"))
                        .addOnFailureListener(e -> Log.d("Nearby", "Advertising failed", e));
            }
        });

        // buttonInit: Assign the admin agent with the voting machine
        buttonInit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO
                Map<String, String> arbitraryDict = new HashMap<>();
                arbitraryDict.put("uTicketType", UTicket.TYPE_INITIALIZATION_UTICKET);
                arbitraryDict.put("deviceId", "noId");
                arbitraryDict.put("holderId", deviceController.getSharedData().getThisPerson().getPersonPubKeyStr());
                deviceController.getFlowIssuerIssueUTicket().issuerIssueUTicketToHerself("noId", arbitraryDict);
                deviceController.getFlowApplyUTicket().holderApplyUTicket("noId");
                // this.iotDevice.getMsgReceiver()._recvXxxMessage();
                // deviceController.getMsgReceiver()._recvXxxMessage();
            }
        });

        // buttonGetData: Get the data from the blockchain(simulated by giving hard-coded data)
        buttonGetData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String candidates = "";
                candidates += "Candidate 1";
                candidates += ", Candidate 2";
                dataFromBlockchain = new HashMap<>();
                dataFromBlockchain.put("Ureka Vote", candidates);
            }
        });

        // buttonApplyInitUTicket: Init the voting machine with the data
        buttonApplyInitUTicket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO
                // TODO: fix the next line
                String targetDeviceId = ""; // = this.iotDevice.getSharedData().getThisDevice().getDevicePubKeyStr();
                String generatedTaskScope = SerializationUtil.dictToJsonStr(Map.of("ALL", "allow"));
                Map<String, String> generatedRequest = Map.of(
                        "deviceId", targetDeviceId,
                        "holderId", deviceController.getSharedData().getThisPerson().getPersonPubKeyStr(),
                        "uTicketType", UTicket.TYPE_SELFACCESS_UTICKET,
                        "taskScope", generatedTaskScope
                );
                deviceController.getFlowIssuerIssueUTicket().issuerIssueUTicketToHerself(targetDeviceId,generatedRequest);

                // Apply UTicket + Challenge-Response
                String generatedCommand = "HELLO-1";
                deviceController.getFlowApplyUTicket().holderApplyUTicket(targetDeviceId,generatedCommand);
//                this.iotDevice.getMsgReceiver()._recvXxxMessage();
//                this.userAgentDO.getMsgReceiver()._recvXxxMessage();
//                this.iotDevice.getMsgReceiver()._recvXxxMessage();
//                this.userAgentDO.getMsgReceiver()._recvXxxMessage();

                // Send UToken
                String data = SerializationUtil.dictToJsonStr(dataFromBlockchain);
                deviceController.getFlowIssueUToken().holderSendCmd(targetDeviceId, data, false);
//                this.iotDevice.getMsgReceiver()._recvXxxMessage();
//                this.userAgentDO.getMsgReceiver()._recvXxxMessage();

                // Access End + receive RTicket
                deviceController.getFlowIssueUToken().holderSendCmd(targetDeviceId, "ACCESS_END", true);
//                this.iotDevice.getMsgReceiver()._recvXxxMessage();
//                this.userAgentDO.getMsgReceiver()._recvXxxMessage();
            }
        });

        // buttonApplyTallyUTicket: Tally the votes
        buttonApplyTallyUTicket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO
                // TODO: fix the next line
                String targetDeviceId = ""; // = this.iotDevice.getSharedData().getThisDevice().getDevicePubKeyStr();
                String generatedTaskScope = SerializationUtil.dictToJsonStr(Map.of("ALL", "allow"));
                Map<String, String> generatedRequest = Map.of(
                        "deviceId", targetDeviceId,
                        "holderId", deviceController.getSharedData().getThisPerson().getPersonPubKeyStr(),
                        "uTicketType", UTicket.TYPE_SELFACCESS_UTICKET,
                        "taskScope", generatedTaskScope
                );
                deviceController.getFlowIssuerIssueUTicket().issuerIssueUTicketToHerself(targetDeviceId,generatedRequest);

                // Apply UTicket + Challenge-Response
                String generatedCommand = "HELLO-1";
                deviceController.getFlowApplyUTicket().holderApplyUTicket(targetDeviceId,generatedCommand);
//                this.iotDevice.getMsgReceiver()._recvXxxMessage();
//                this.userAgentDO.getMsgReceiver()._recvXxxMessage();
//                this.iotDevice.getMsgReceiver()._recvXxxMessage();
//                this.userAgentDO.getMsgReceiver()._recvXxxMessage();

                // Send UToken
                String data = SerializationUtil.dictToJsonStr(dataFromBlockchain);
                deviceController.getFlowIssueUToken().holderSendCmd(targetDeviceId, data, false);
//                this.iotDevice.getMsgReceiver()._recvXxxMessage();
//                this.userAgentDO.getMsgReceiver()._recvXxxMessage();

                // Access End + receive RTicket
                deviceController.getFlowIssueUToken().holderSendCmd(targetDeviceId, "ACCESS_END", true);
//                this.iotDevice.getMsgReceiver()._recvXxxMessage();
//                this.userAgentDO.getMsgReceiver()._recvXxxMessage();
            }
        });

        // buttonShowRTickets: Show the RTicket received from the voting machine
        buttonShowRTickets.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO
            }
        });
    }

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

                buttonInit.setEnabled(true);
                buttonGetData.setEnabled(true);
                buttonApplyInitUTicket.setEnabled(true);
                buttonApplyTallyUTicket.setEnabled(true);
                buttonShowRTickets.setEnabled(true);

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