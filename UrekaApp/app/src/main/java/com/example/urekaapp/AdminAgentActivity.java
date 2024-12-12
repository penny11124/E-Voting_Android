package com.example.urekaapp;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Telephony;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
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

import org.bouncycastle.mime.smime.SMimeMultipartContext;
import org.checkerframework.checker.units.qual.A;
import org.junit.platform.commons.util.StringUtils;

import java.io.Serial;
import java.io.Serializable;
import java.security.interfaces.ECPublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import ureka.framework.Environment;
import ureka.framework.logic.DeviceController;
import ureka.framework.model.data_model.OtherDevice;
import ureka.framework.model.data_model.ThisDevice;
import ureka.framework.model.message_model.UTicket;
import ureka.framework.resource.crypto.SerializationUtil;
import ureka.framework.resource.logger.SimpleLogger;

public class AdminAgentActivity extends AppCompatActivity {
    private DeviceController deviceController;

    // Data
    private ArrayList<String> candidates; // The names of the candidates
    private ArrayList<Integer> candidateVotes; // The votes of the candidates
    private ArrayList<String> voters; // The public key of the voters
    private ArrayList<Boolean> voterVoted; // Whether the voters had voted
    public static String connectedDeviceId; // The device_id of the voting machine

    // Components
    private Button buttonScan;
    private Button buttonAdvertising;
    private Button buttonInit;
    private Button buttonGetData;
    private Button buttonApplyInitUTicket;
    private Button buttonApplyTallyUTicket;
    private Button buttonShowRTickets;
    private TextView textViewConnectingStatus;
    private Button buttonDisconnect;

    // Bluetooth connection

    // Queuing ticket sending
    public static boolean sendNextTicket = false; // Whether to send the next ticket: true when the previous ticket finished

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
        if (!NearbyPermissionHelper.hasPermissions(this)) {
            NearbyPermissionHelper.requestPermissions(this);
        }

        // private fields initialization
        deviceController = new DeviceController(ThisDevice.USER_AGENT_OR_CLOUD_SERVER, "Admin Agent");
        deviceController.getExecutor()._executeOneTimeInitializeAgentOrServer();
        deviceController.getNearbyManager().setMsgReceiver(deviceController.getMsgReceiver());

        buttonScan = findViewById(R.id.buttonScan);
        buttonAdvertising = findViewById(R.id.buttonAdvertising);
        buttonInit = findViewById(R.id.buttonInit);
        buttonGetData = findViewById(R.id.buttonGetData);
        buttonApplyInitUTicket = findViewById(R.id.buttonApplyInitUTicket);
        buttonApplyTallyUTicket = findViewById(R.id.buttonApplyTallyUTicket);
        buttonShowRTickets = findViewById(R.id.buttonShowRTickets);
        textViewConnectingStatus = findViewById(R.id.textViewConnectingStatus);
        buttonDisconnect = findViewById(R.id.buttonDisconnect);
        String mode = getIntent().getStringExtra("mode");
        if (!Objects.equals(mode, "TEST")) {
            buttonInit.setEnabled(false);
            buttonGetData.setEnabled(false);
            buttonApplyInitUTicket.setEnabled(false);
            buttonApplyTallyUTicket.setEnabled(false);
            buttonShowRTickets.setEnabled(false);
        };
        deviceController.getNearbyViewModel().getIsConnected().observe(this, isConnected -> {
            SimpleLogger.simpleLog("info", "AdminAgentActivity: isConnected = " + isConnected);
            if (isConnected != null && isConnected) {
                textViewConnectingStatus.setText("Connected to Voter Agent");
            } else {
                textViewConnectingStatus.setText("Not connected");
            }
        });

        // buttonScan: Connect to the voting machine
        buttonScan.setOnClickListener(view -> {
            deviceController.getNearbyViewModel().getNearbyManager(Environment.applicationContext,deviceController.getMsgReceiver()).stopAllActions();
            deviceController.connectToDevice("HC-04BLE",
                    () -> runOnUiThread(() -> {
                        Toast.makeText(AdminAgentActivity.this, "Device connected!", Toast.LENGTH_SHORT).show();
                        buttonInit.setEnabled(true);
                        buttonGetData.setEnabled(true);
                    }),
                    () -> runOnUiThread(() -> {
                        Toast.makeText(AdminAgentActivity.this, "Device disconnected!", Toast.LENGTH_SHORT).show();
                        buttonInit.setEnabled(false);
                        buttonGetData.setEnabled(false);
                        buttonApplyInitUTicket.setEnabled(false);
                        buttonApplyTallyUTicket.setEnabled(false);
                    }),
                    textViewConnectingStatus
            );
        });

        // buttonAdvertising: Start advertising for the voter agent
        buttonAdvertising.setOnClickListener(view -> {
            deviceController.getBleManager().disconnect();
            deviceController.getNearbyViewModel().getNearbyManager(Environment.applicationContext, deviceController.getMsgReceiver()).startAdvertising();
        });

        // buttonInit: Assign the admin agent with the voting machine
        buttonInit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if the device table is empty, i.e. uninitialized
                if (!deviceController.getFlowApplyUTicket().getReceivedMsgStorer().getSharedData().getDeviceTable().isEmpty()) {
                    String errorMessage = "Device Table is not empty";
                    Toast.makeText(v.getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                    return;
                }

                Map<String, String> arbitraryDict = new HashMap<>();
                arbitraryDict.put("u_ticket_type", UTicket.TYPE_INITIALIZATION_UTICKET);
                arbitraryDict.put("device_id", "no_id");
                arbitraryDict.put("holder_id", deviceController.getSharedData().getThisPerson().getPersonPubKeyStr());
                deviceController.getFlowIssuerIssueUTicket().issuerIssueUTicketToHerself("no_id", arbitraryDict);
                deviceController.getFlowApplyUTicket().holderApplyUTicket("no_id");

                while (!sendNextTicket) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                buttonApplyInitUTicket.setEnabled(true);
            }
        });

        // buttonGetData: Get the data from the blockchain(simulated by giving hard-coded data)
        buttonGetData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                candidates = new ArrayList<>();
                candidates.add("Alice");
                candidates.add("Bob");

                voters = new ArrayList<>();
                voters.add(getIntent().getStringExtra("key1"));
//                voters.add(getIntent().getStringExtra("key2"));
            }
        });

        // buttonApplyInitUTicket: Init the voting machine with the data
        buttonApplyInitUTicket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String generatedTaskScope = "{\"ALL\": \"allow\"}";
                Map<String, String> generatedRequest = Map.of(
                        "device_id", connectedDeviceId,
                        "holder_id", deviceController.getSharedData().getThisPerson().getPersonPubKeyStr(),
                        "u_ticket_type", UTicket.TYPE_SELFACCESS_UTICKET,
                        "task_scope", generatedTaskScope
                );
                deviceController.getFlowIssuerIssueUTicket().issuerIssueUTicketToHerself(connectedDeviceId, generatedRequest);

                // Apply UTicket + CRKE
                sendNextTicket = false;
                String generatedCommand = "HELLO-1";
                deviceController.getFlowApplyUTicket().holderApplyUTicket(connectedDeviceId, generatedCommand);

                // Send UToken: CC
                while (!sendNextTicket) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                sendNextTicket = false;
                deviceController.getFlowIssueUToken().holderSendCmd(connectedDeviceId, "CC", false);

                // Send UToken: candidates and voters
                for (String candidate: candidates) {
                    while (!sendNextTicket) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    sendNextTicket = false;
                    String data = "C:" + candidate;
                    deviceController.getFlowIssueUToken().holderSendCmd(connectedDeviceId, data, false);
                }
                for (String voter: voters) {
                    while (!sendNextTicket) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    sendNextTicket = false;
                    String data = "C-" + voter;
                    deviceController.getFlowIssueUToken().holderSendCmd(connectedDeviceId, data, false);
                }

                // Access End + receive RTicket
                while (!sendNextTicket) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                sendNextTicket = false;
                deviceController.getFlowIssueUToken().holderSendCmd(connectedDeviceId, "ACCESS_END_C", true);

                while (!sendNextTicket) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                buttonApplyTallyUTicket.setEnabled(true);
            }
        });

        // buttonApplyTallyUTicket: Tally the votes
        buttonApplyTallyUTicket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String generatedTaskScope = "{\"ALL\": \"allow\"}";
                Map<String, String> generatedRequest = Map.of(
                        "device_id", connectedDeviceId,
                        "holder_id", deviceController.getSharedData().getThisPerson().getPersonPubKeyStr(),
                        "u_ticket_type", UTicket.TYPE_SELFACCESS_UTICKET,
                        "task_scope", generatedTaskScope
                );
                deviceController.getFlowIssuerIssueUTicket().issuerIssueUTicketToHerself(connectedDeviceId,generatedRequest);

                // Apply UTicket + CRKE
                sendNextTicket = false;
                String generatedCommand = "HELLO-1";
                SimpleLogger.simpleLog("info", "AdminAgentActivity: Sending HELLO-1");
                deviceController.getFlowApplyUTicket().holderApplyUTicket(connectedDeviceId,generatedCommand);

                // Send UToken: the votes of candidates
                while (!sendNextTicket) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                sendNextTicket = false;
                String data = "TC";
                SimpleLogger.simpleLog("info", "AdminAgentActivity: Sending TC");
                deviceController.getFlowIssueUToken().holderSendCmd(connectedDeviceId, data, false);
                while (!sendNextTicket) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                data = deviceController.getFlowIssueUToken().getExecutor().getSharedData().getCurrentSession().getAssociatedPlaintextData();
                data = data.substring("DATA: ".length());

                candidateVotes = new ArrayList<>();
                String[] result = data.split(":");
                for (int i = 0; i < result.length; i+= 2) {
                    try {
                        int index = Integer.parseInt(result[i]);
                        String[] candidateInfo = result[i + 1].split(",");
                        if (!Objects.equals(candidates.get(index), candidateInfo[0])) {
                            // if the name of the candidate doesn't match
                            throw new RuntimeException("The name of the candidate doesn't match");
                        } else {
                            candidateVotes.add(Integer.parseInt(candidateInfo[1]));
                        }
                    } catch (NumberFormatException e) {
                        throw new RuntimeException(e);
                    }
                }

                // Send UToken: if the voters had voted
                voterVoted = new ArrayList<>();
                for (int i = 0;; i++) {
                    data = "TV" + i;
                    sendNextTicket = false;
                    SimpleLogger.simpleLog("info", "AdminAgentActivity: Sending " + data);
                    deviceController.getFlowIssueUToken().holderSendCmd(connectedDeviceId, data, false);

                    while (!sendNextTicket) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    data = deviceController.getFlowIssueUToken().getExecutor().getSharedData().getCurrentSession().getAssociatedPlaintextData();
                    data = data.substring("DATA: ".length());
                    SimpleLogger.simpleLog("info", "Voter " + i + " DATA = " + data);
                    if (Objects.equals(data, "-----")) {
                        break;
                    }

                    result = data.split(":");
                    if (!Objects.equals(String.valueOf(i), result[0])) {
                        // if the public key of the voter doesn't match
                        throw new RuntimeException("The public key of the voter doesn't match");
                    } else {
                        if (Objects.equals(result[1], "0")) {
                            voterVoted.add(false);
                        } else {
                            voterVoted.add(true);
                        }
                    }
                }

                // Access End + receive RTicket
                sendNextTicket = false;
                SimpleLogger.simpleLog("info", "AdminAgentActivity: Sending ACCESS_END");
                deviceController.getFlowIssueUToken().holderSendCmd(connectedDeviceId, "ACCESS_END_T", true);

                while (!sendNextTicket) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                buttonShowRTickets.setEnabled(true);
            }
        });

        // buttonShowRTickets: Show the RTicket received from the voting machine
        buttonShowRTickets.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminAgentActivity.this, AdminAgentResultActivity.class);

                Map<String, Integer> candidateVotesMap = new HashMap<>();
                for (int i = 0; i < candidates.size(); i++) {
                    candidateVotesMap.put(candidates.get(i), candidateVotes.get(i));
                }

                ArrayList<String> rticketList = new ArrayList<>();
                for (int i = 0; i < voters.size(); i++) {
                    if (voterVoted.get(i)) {
                        rticketList.add(voters.get(i));
                    }
                }
                intent.putExtra("mapSerializable", (Serializable) candidateVotesMap);
                intent.putStringArrayListExtra("RTICKET_LIST", rticketList);
                startActivity(intent);
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