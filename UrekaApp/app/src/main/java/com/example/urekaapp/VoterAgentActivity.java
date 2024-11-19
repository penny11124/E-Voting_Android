package com.example.urekaapp;

import android.app.Activity;
import android.content.Intent;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.urekaapp.ble.BLEManager;
import com.example.urekaapp.ble.BLEPermissionHelper;
import com.example.urekaapp.ble.BLEViewModel;
import com.example.urekaapp.communication.NearbyManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import ureka.framework.Environment;
import ureka.framework.logic.DeviceController;
import ureka.framework.model.data_model.ThisDevice;
import ureka.framework.model.message_model.UTicket;
import ureka.framework.resource.crypto.SerializationUtil;

public class VoterAgentActivity extends AppCompatActivity {
    private DeviceController deviceController;
    private String connectedDeviceId; // The deviceId of the voting machine
    private int votedCandidate;

    private Button buttonScan = findViewById(R.id.buttonScanDevice);
    private Button buttonConnect = findViewById(R.id.buttonConnect);
    private final Button buttonRequestUTicket = findViewById(R.id.buttonRequestUTicket);
    private final Button buttonApplyUTicket = findViewById(R.id.buttonApplyUTicket);
    private final Button buttonShowRTicket = findViewById(R.id.buttonShowRTicket);

    private final ActivityResultLauncher<Intent> activityResultLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        votedCandidate = data.getIntExtra("VOTED_CANDIDATE", -1);
                        buttonApplyUTicket.setEnabled(false);
                        buttonShowRTicket.setEnabled(true);
                    }

                    if (votedCandidate != -1) {
                        String cmd = "V:" + votedCandidate;
                        deviceController.getFlowIssueUToken().holderSendCmd(connectedDeviceId, cmd, false);
//                        this.iotDevice.getMsgReceiver()._recvXxxMessage();
//                        this.userAgentDO.getMsgReceiver()._recvXxxMessage();
                    } else {
                        throw new RuntimeException("Invalid vote.");
                    }
                }
            });



    private BLEViewModel bleViewModel;
    private NearbyManager nearbyManager;

    @SuppressLint("MissingInflatedId")
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

        // private fields initialization
        if (!BLEPermissionHelper.hasPermissions(this)) {
            BLEPermissionHelper.requestPermissions(this);
        }

        bleViewModel = new ViewModelProvider(this).get(BLEViewModel.class);
        nearbyManager = new NearbyManager(this);

        deviceController = new DeviceController(ThisDevice.USER_AGENT_OR_CLOUD_SERVER, "User Agent");
        deviceController.getExecutor()._executeOneTimeInitializeAgentOrServer();
        nearbyManager.setMsgReceiver(deviceController.getMsgReceiver());

        // components initialization
        String mode = getIntent().getStringExtra("mode");
        if (!Objects.equals(mode, "TEST")) {
            buttonRequestUTicket.setEnabled(false);
            buttonApplyUTicket.setEnabled(false);
            buttonShowRTicket.setEnabled(false);
        }

        buttonConnect.setOnClickListener(view -> {
            bleViewModel.getBLEManager(Environment.applicationContext).disconnect();
            nearbyManager.startDiscovery("VoterAgent");
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
                deviceController.getFlowIssuerIssueUTicket().issuerIssueUTicketToHolder(targetDeviceId,generatedRequest);
//                deviceController.getMsgReceiver()._recvXxxMessage();
            }
        });

        buttonScan.setOnClickListener(view -> {
            nearbyManager.stopAllActions();
            deviceController.connectToDevice("HC-04BLE",
                    () -> runOnUiThread(() -> {
                        Toast.makeText(VoterAgentActivity.this, "Device connected!", Toast.LENGTH_SHORT).show();
                        buttonApplyUTicket.setEnabled(true);
                    }),
                    () -> runOnUiThread(() -> {
                        Toast.makeText(VoterAgentActivity.this, "Device disconnected!", Toast.LENGTH_SHORT).show();
                        buttonApplyUTicket.setEnabled(false);
                    })
            );
        });

        buttonApplyUTicket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO
                String targetDeviceId = connectedDeviceId; // = this.iotDevice.getSharedData().getThisDevice().getDevicePubKeyStr();
                String generatedCommand = "HELLO-1";
                deviceController.getFlowApplyUTicket().holderApplyUTicket(targetDeviceId,generatedCommand);
//                this.iotDevice.getMsgReceiver()._recvXxxMessage();
//                this.cloudServerEP.getMsgReceiver()._recvXxxMessage();
//                this.iotDevice.getMsgReceiver()._recvXxxMessage();
//                this.cloudServerEP.getMsgReceiver()._recvXxxMessage();

                String data = "A";
                deviceController.getFlowIssueUToken().holderSendCmd(connectedDeviceId, data, false);
//                this.iotDevice.getMsgReceiver()._recvXxxMessage();
//                this.userAgentDO.getMsgReceiver()._recvXxxMessage();
                data = deviceController.getFlowIssueUToken().getExecutor().getSharedData().getCurrentSession().getPlaintextData();
                data = data.replaceAll("[0-9]", "");
                String[] result = data.split("\\.");
                ArrayList<String> candidates = new ArrayList<>();
                Collections.addAll(candidates, result);

                Intent intent = new Intent(VoterAgentActivity.this, VoterAgentVotingActivity.class);
                intent.putStringArrayListExtra("CANDIDATES", candidates);
                activityResultLauncher.launch(intent);
            }
        });

        buttonShowRTicket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(VoterAgentActivity.this, VoterAgentResultActivity.class);

                intent.putExtra("PUBLIC_KEY", deviceController.getExecutor().getSharedData().getThisDevice().getDevicePubKeyStr());
                startActivity(intent);
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