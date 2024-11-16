package com.example.urekaapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.urekaapp.communication.BLEManager;
import com.example.urekaapp.communication.BLEPermissionHelper;

import java.util.HashMap;
import java.util.Map;

import ureka.framework.logic.DeviceController;
import ureka.framework.model.data_model.ThisDevice;
import ureka.framework.model.message_model.UTicket;
import ureka.framework.resource.crypto.SerializationUtil;

public class AdminAgentActivity extends AppCompatActivity {
    private Map<String, String> dataFromBlockchain; // Assume we only have 1 vote
    private DeviceController deviceController;
//    private KeyStore keyStore;


    private Button buttonScan;
    private Button buttonInit;
    private Button buttonGetData;
    private Button buttonApplyInitUTicket;
    private Button buttonApplyTallyUTicket;
    private Button buttonShowRTickets;

    private BLEViewModel bleViewModel;

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

        if (!BLEPermissionHelper.hasPermissions(this)) {
            BLEPermissionHelper.requestPermissions(this);
        }

        bleViewModel = new ViewModelProvider(this).get(BLEViewModel.class);

        // private fields initialization
        deviceController = new DeviceController(ThisDevice.USER_AGENT_OR_CLOUD_SERVER, "Admin Agent");
        deviceController.getExecutor()._executeOneTimeInitializeAgentOrServer();

        // components initialization
        buttonScan = findViewById(R.id.buttonScan);
        buttonInit = findViewById(R.id.buttonInit);
        buttonGetData = findViewById(R.id.buttonGetData);
        buttonApplyInitUTicket = findViewById(R.id.buttonApplyInitUTicket);
        buttonApplyTallyUTicket = findViewById(R.id.buttonApplyTallyUTicket);
        buttonShowRTickets = findViewById(R.id.buttonShowRTickets);
        buttonInit.setEnabled(false);
        buttonGetData.setEnabled(false);
        buttonApplyInitUTicket.setEnabled(false);
        buttonApplyTallyUTicket.setEnabled(false);
        buttonShowRTickets.setEnabled(false);

        buttonScan.setOnClickListener(view -> {
            deviceController.connectToDevice("YourDeviceName",
                    () -> runOnUiThread(() -> {
                        Toast.makeText(AdminAgentActivity.this, "Device connected!", Toast.LENGTH_SHORT).show();
                        buttonInit.setEnabled(true);
                        buttonGetData.setEnabled(true);
                    }),
                    () -> runOnUiThread(() -> {
                        Toast.makeText(AdminAgentActivity.this, "Device disconnected!", Toast.LENGTH_SHORT).show();
                        buttonInit.setEnabled(false);
                        buttonGetData.setEnabled(false);
                    })
            );
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        BLEPermissionHelper.handlePermissionResult(this, requestCode, permissions, grantResults);

        if (BLEPermissionHelper.hasPermissions(this)) {
            Toast.makeText(this, "Permissions granted, you can now use BLE features.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bleViewModel != null) {
            BLEManager bleManager = bleViewModel.getBLEManager(this);
            if (bleManager != null) {
                bleManager.disconnect();
            }
        }
    }
}