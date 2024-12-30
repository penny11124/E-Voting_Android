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
import ureka.framework.model.message_model.RTicket;
import ureka.framework.model.message_model.UTicket;
import ureka.framework.resource.crypto.SerializationUtil;
import ureka.framework.resource.logger.SimpleLogger;

public class VoterAgentActivity extends AppCompatActivity {
    private DeviceController deviceController;
    public static String connectedDeviceId; // The device_id of the voting machine
    public static String permissionlessData;
    private int votedCandidate;

    private TextView textViewConnectingStatus;
    private Button buttonConnect;
    private Button buttonRequestUTicket;
    private Button buttonScan;
    private Button buttonApplyUTicket;
    private Button buttonPermissionlessVoter;
    private Button buttonShowRTicket;
    private Button buttonSendRTicket;
    private Button buttonDisconnect;

    public static boolean sendNextTicket = false;

    private final ActivityResultLauncher<Intent> activityResultLauncher =
        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            long startTime = System.nanoTime();
            if (result.getResultCode() == Activity.RESULT_OK) {
                Intent data = result.getData();
                if (data != null) {
                    votedCandidate = data.getIntExtra("VOTED_CANDIDATE", -1);
                }

                if (votedCandidate != -1) {
                    sendNextTicket = false;
                    String cmd = "V:" + votedCandidate;
                    deviceController.getFlowIssueUToken().holderSendCmd(connectedDeviceId, cmd, false);
                    while (!sendNextTicket) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    sendNextTicket = false;
                    deviceController.getFlowIssueUToken().holderSendCmd(connectedDeviceId, "ACCESS_END", true);
                    while (!sendNextTicket) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    buttonApplyUTicket.setEnabled(false);
                    buttonShowRTicket.setEnabled(true);
                    buttonSendRTicket.setEnabled(true);
                } else {
                    throw new RuntimeException("Invalid vote.");
                }
            }
            long endTime = System.nanoTime();
            long duration = endTime - startTime;
            SimpleLogger.simpleLog("info", "VoterAgentActivity: buttonApplyUTicket(PostVote) execution time: " + duration + " ns");
        });


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
        deviceController = new DeviceController(ThisDevice.USER_AGENT_OR_CLOUD_SERVER, "User Agent");
        deviceController.getExecutor()._executeOneTimeInitializeAgentOrServer();
        try {
            deviceController.getSharedData().getThisPerson().setPersonPubKey(SerializationUtil.base64ToPublicKey(getIntent().getStringExtra("publicKey")));
            deviceController.getSharedData().getThisPerson().setPersonPrivKey(SerializationUtil.base64ToPrivateKey(getIntent().getStringExtra("privateKey")));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        deviceController.getNearbyManager().setMsgReceiver(deviceController.getMsgReceiver());
        SimpleLogger.simpleLog("info","MsgReceiver: "+(deviceController.getMsgReceiver()==null));

        textViewConnectingStatus = findViewById(R.id.textViewConnectingStatus);
        buttonConnect = findViewById(R.id.buttonConnect);
        buttonRequestUTicket = findViewById(R.id.buttonRequestUTicket);
        buttonScan = findViewById(R.id.buttonScanDevice);
        buttonApplyUTicket = findViewById(R.id.buttonApplyUTicket);
        buttonPermissionlessVoter = findViewById(R.id.buttonPermissionlessVoter);
        buttonShowRTicket = findViewById(R.id.buttonShowRTicket);
        buttonSendRTicket = findViewById(R.id.buttonSendRTicket);
        buttonDisconnect = findViewById(R.id.buttonDisconnect2);

        buttonRequestUTicket.setEnabled(false);
        buttonApplyUTicket.setEnabled(false);
        buttonPermissionlessVoter.setEnabled(false);
        buttonShowRTicket.setEnabled(false);
        buttonSendRTicket.setEnabled(false);

        deviceController.getNearbyViewModel().getIsConnected().observe(this, isConnected -> {
            if (isConnected != null && isConnected) {
                buttonRequestUTicket.setEnabled(true);
                textViewConnectingStatus.setText("Connected to Admin Agent");
            } else {
                buttonRequestUTicket.setEnabled(false);
                textViewConnectingStatus.setText("Not connected");
            }
        });

        deviceController.getBleViewModel().getIsConnected().observe(this, isConnected -> {
            SimpleLogger.simpleLog("info", "VoterAgentActivity: Voter Agent isConnected = " + isConnected);
            if (isConnected != null && isConnected) {
                textViewConnectingStatus.setText("Voter Agent connected to VM");
            } else {
                textViewConnectingStatus.setText("Not connected");
            }
        });

        buttonConnect.setOnClickListener(view -> {
            if (deviceController.getBleManager()!= null && deviceController.getBleManager().isConnected()) {
                deviceController.getBleManager().getConnectionState().observe(VoterAgentActivity.this, isConnected -> {
                    if (isConnected != null && !isConnected) {
                        deviceController.getNearbyViewModel().getNearbyManager(Environment.applicationContext, deviceController.getMsgReceiver()).startDiscovery();
                        deviceController.getBleManager().getConnectionState().removeObservers(VoterAgentActivity.this);
                    }
                });

                deviceController.getBleManager().disconnect();
            } else {
                deviceController.getNearbyViewModel().getNearbyManager(Environment.applicationContext, deviceController.getMsgReceiver()).startDiscovery();
            }
        });

        buttonRequestUTicket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long startTime = System.nanoTime();
                String generatedTaskScope = "{\"ALL\": \"allow\"}";
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
                long endTime = System.nanoTime();
                long duration = endTime - startTime;
                SimpleLogger.simpleLog("info", "VoterAgentActivity: buttonRequestUTicket execution time: " + duration + " ns");
            }
        });

        buttonScan.setOnClickListener(view -> {
            deviceController.getNearbyViewModel().getNearbyManager(Environment.applicationContext, deviceController.getMsgReceiver()).stopAllActions();
            deviceController.connectToDevice("HC-04BLE",
                    () -> runOnUiThread(() -> {
                        Toast.makeText(VoterAgentActivity.this, "Device connected!", Toast.LENGTH_SHORT).show();
                        buttonApplyUTicket.setEnabled(true);
                        buttonPermissionlessVoter.setEnabled(true);
                    }),
                    () -> runOnUiThread(() -> {
                        Toast.makeText(VoterAgentActivity.this, "Device disconnected!", Toast.LENGTH_SHORT).show();
                        buttonApplyUTicket.setEnabled(false);
                        buttonPermissionlessVoter.setEnabled(false);
                    }),
                    textViewConnectingStatus
            );
        });

        buttonApplyUTicket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long startTime = System.nanoTime();
                String targetDeviceId = connectedDeviceId;
                String generatedCommand = "HELLO-1";
                deviceController.getFlowApplyUTicket().holderApplyUTicket(targetDeviceId,generatedCommand);

                while (!sendNextTicket) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                sendNextTicket = false;
                String data = "A";
                deviceController.getFlowIssueUToken().holderSendCmd(connectedDeviceId, data, false);
                while (!sendNextTicket || deviceController.getFlowIssueUToken().getExecutor().getSharedData().getCurrentSession().getPlaintextData() == null) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                data = deviceController.getFlowIssueUToken().getExecutor().getSharedData().getCurrentSession().getPlaintextData();
                SimpleLogger.simpleLog("info", "raw DATA = " + data);
                String[] result = data.split(":");
                ArrayList<String> candidates = new ArrayList<>();
                for (int i = 1; i < result.length; i += 2) {
                    candidates.add(result[i]);
                }
                SimpleLogger.simpleLog("info", "candidate list = " + candidates);

                long endTime = System.nanoTime();
                long duration = endTime - startTime;
                SimpleLogger.simpleLog("info", "VoterAgentActivity: buttonApplyUTicket(PreVote) execution time: " + duration + " ns");
                Intent intent = new Intent(VoterAgentActivity.this, VoterAgentVotingActivity.class);
                intent.putStringArrayListExtra("CANDIDATES", candidates);
                activityResultLauncher.launch(intent);
            }
        });

        buttonPermissionlessVoter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    long startTime = System.nanoTime();
                    sendNextTicket = false;
                    deviceController.getMsgSender().sendXxxMessage(
                            Message.MESSAGE_PERMISSIONLESS,
                            Message.MESSAGE_PERMISSIONLESS,
                            ""
                    );
                    while (!sendNextTicket) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    Map<String, String> data = SerializationUtil.jsonToMap(permissionlessData);
                    ArrayList<String> candidatesList = new ArrayList<>(), votersList = new ArrayList<>();
                    for (String key : data.keySet()) {
                        if (key.contains("candidate")) {
                            candidatesList.add(data.get(key));
                        } else if (key.contains("voter")) {
                            votersList.add(data.get(key));
                        }
                    }
                    long endTime = System.nanoTime();
                    long duration = endTime - startTime;
                    SimpleLogger.simpleLog("info", "VoterAgentActivity: buttonPermissionlessVoter execution time: " + duration + " ns");
                    Intent intent = new Intent(VoterAgentActivity.this, PermissionlessActivity.class);
                    intent.putStringArrayListExtra("candidatesList", candidatesList);
                    intent.putStringArrayListExtra("votersList", votersList);
                    startActivity(intent);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        buttonShowRTicket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(VoterAgentActivity.this, VoterAgentResultActivity.class);

                intent.putExtra("PUBLIC_KEY", deviceController.getSharedData().getThisPerson().getPersonPubKeyStr());
                startActivity(intent);
            }
        });

        buttonSendRTicket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long startTime = System.nanoTime();
                sendNextTicket = false;
                deviceController.getFlowIssuerIssueUTicket().holderSendRTicketToIssuer(connectedDeviceId);
                while (!sendNextTicket) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                long endTime = System.nanoTime();
                long duration = endTime - startTime;
                SimpleLogger.simpleLog("info", "VoterAgentActivity: buttonSendRTicket execution time: " + duration + " ns");
            }
        });

        buttonDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deviceController.getBleManager().disconnect();
                deviceController.getNearbyManager().stopAllActions();
                deviceController.getNearbyManager().disconnectFromAllEndpoints();
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
        BLEManager bleManager = deviceController.getBleManager();
        if (bleManager != null) {
            bleManager.disconnect();
        }
    }
}