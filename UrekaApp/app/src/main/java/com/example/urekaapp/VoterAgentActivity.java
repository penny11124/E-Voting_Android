package com.example.urekaapp;

import android.app.Activity;
import android.content.Intent;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
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
import com.example.urekaapp.communication.NearbyPermissionHelper;
import com.example.urekaapp.communication.NearbyViewModel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import ureka.framework.Environment;
import ureka.framework.logic.DeviceController;
import ureka.framework.model.data_model.ThisDevice;
import ureka.framework.model.message_model.Message;
import ureka.framework.model.message_model.UTicket;
import ureka.framework.resource.crypto.SerializationUtil;

public class VoterAgentActivity extends AppCompatActivity {
    private DeviceController deviceController;
    private String connectedDeviceId; // The device_id of the voting machine
    private int votedCandidate;

    private Button buttonScan;
    private Button buttonConnect;
    private Button buttonRequestUTicket;
    private Button buttonApplyUTicket;
    private Button buttonShowRTicket;
    private TextView textViewConnectingStatus;

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
    private NearbyViewModel nearbyViewModel;

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
        if (!NearbyPermissionHelper.hasPermissions(this)) {
            NearbyPermissionHelper.requestPermissions(this);
        }

        // private fields initialization
        bleViewModel = new ViewModelProvider(this).get(BLEViewModel.class);
        nearbyViewModel = new ViewModelProvider(this).get(NearbyViewModel.class);

        deviceController = new DeviceController(ThisDevice.USER_AGENT_OR_CLOUD_SERVER, "User Agent");
        deviceController.getExecutor()._executeOneTimeInitializeAgentOrServer();
        bleViewModel = new ViewModelProvider(this).get(BLEViewModel.class);
        nearbyViewModel.getNearbyManager(Environment.applicationContext,deviceController.getMsgReceiver()).setViewModel(nearbyViewModel);

        buttonScan = findViewById(R.id.buttonScanDevice);
        buttonConnect = findViewById(R.id.buttonConnect);
        buttonRequestUTicket = findViewById(R.id.buttonRequestUTicket);
        buttonApplyUTicket = findViewById(R.id.buttonApplyUTicket);
        buttonShowRTicket = findViewById(R.id.buttonShowRTicket);
        textViewConnectingStatus = findViewById(R.id.textViewConnectingStatus);
        String mode = getIntent().getStringExtra("mode");
        if (!Objects.equals(mode, "TEST")) {
            buttonRequestUTicket.setEnabled(false);
            buttonApplyUTicket.setEnabled(false);
            buttonShowRTicket.setEnabled(false);
        }
        nearbyViewModel.getIsConnected().observe(this, isConnected -> {
            if (isConnected != null && isConnected) {
                buttonRequestUTicket.setEnabled(true);
                textViewConnectingStatus.setText("Connected to Admin Agent");
            } else {
                buttonRequestUTicket.setEnabled(false);
                textViewConnectingStatus.setText("Not connected");
            }
        });

        buttonConnect.setOnClickListener(view -> {
            if (bleViewModel.getBLEManager(Environment.applicationContext) != null && bleViewModel.getBLEManager(Environment.applicationContext).isConnected()) {
                bleViewModel.getBLEManager(Environment.applicationContext).getConnectionState().observe(VoterAgentActivity.this, isConnected -> {
                    if (isConnected != null && !isConnected) {
                        nearbyViewModel.getNearbyManager(Environment.applicationContext, deviceController.getMsgReceiver()).startDiscovery();
                        bleViewModel.getBLEManager(Environment.applicationContext).getConnectionState().removeObservers(VoterAgentActivity.this);
                    }
                });

                bleViewModel.getBLEManager(Environment.applicationContext).disconnect();
            } else {
                nearbyViewModel.getNearbyManager(Environment.applicationContext, deviceController.getMsgReceiver()).startDiscovery();
            }
        });

        buttonRequestUTicket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String generatedTaskScope = SerializationUtil.mapToJson(Map.of("ALL", "allow"));
                Map<String, String> generatedRequest = Map.of(
                        "holder_id", deviceController.getSharedData().getThisPerson().getPersonPubKeyStr(),
                        "u_ticket_type", UTicket.TYPE_ACCESS_UTICKET,
                        "task_scope", generatedTaskScope
                );
                try {
                    deviceController.getMsgSender().sendXxxMessageByNearby(
                            Message.MESSAGE_REQUEST,
                            Message.MESSAGE_REQUEST,
                            SerializationUtil.mapToJson(generatedRequest)
                    );
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        buttonScan.setOnClickListener(view -> {
            nearbyViewModel.getNearbyManager(Environment.applicationContext, deviceController.getMsgReceiver()).stopAllActions();
            deviceController.connectToDevice("HC-04BLE",
                    () -> runOnUiThread(() -> {
                        Toast.makeText(VoterAgentActivity.this, "Device connected!", Toast.LENGTH_SHORT).show();
                        buttonApplyUTicket.setEnabled(true);
                    }),
                    () -> runOnUiThread(() -> {
                        Toast.makeText(VoterAgentActivity.this, "Device disconnected!", Toast.LENGTH_SHORT).show();
                        buttonApplyUTicket.setEnabled(false);
                    }),
                    textViewConnectingStatus
            );
        });

        buttonApplyUTicket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String targetDeviceId = connectedDeviceId;
                String generatedCommand = "HELLO-1";
                deviceController.getFlowApplyUTicket().holderApplyUTicket(targetDeviceId,generatedCommand);

                String data = "A";
                deviceController.getFlowIssueUToken().holderSendCmd(connectedDeviceId, data, false);
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

        // Register a callback to handle back button presses
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {}
        };
        // Add the callback to the dispatcher
        getOnBackPressedDispatcher().addCallback(this, callback);
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