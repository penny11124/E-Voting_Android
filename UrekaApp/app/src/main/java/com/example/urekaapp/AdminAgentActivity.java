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
import java.security.KeyPair;
import java.security.interfaces.ECPublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import ureka.framework.Environment;
import ureka.framework.logic.DeviceController;
import ureka.framework.model.data_model.OtherDevice;
import ureka.framework.model.data_model.ThisDevice;
import ureka.framework.model.message_model.Message;
import ureka.framework.model.message_model.UTicket;
import ureka.framework.resource.crypto.ECC;
import ureka.framework.resource.crypto.SerializationUtil;
import ureka.framework.resource.logger.SimpleLogger;

public class AdminAgentActivity extends AppCompatActivity {
    private DeviceController deviceController;
    private DeviceController manufacturerController;

    // Data
    private ArrayList<String> candidates; // The names of the candidates
    private ArrayList<Integer> candidateVotes; // The votes of the candidates
    private ArrayList<String> voters; // The public key of the voters
    private ArrayList<Boolean> voterVoted; // Whether the voters had voted
    public static String connectedDeviceId; // The device_id of the voting machine
    public static String permissionlessData; // The data received from Permissionless RTicket

    // Components
    private TextView textViewConnectingStatus;
    private Button buttonScanManufacturer;
    private Button buttonScan;
    private Button buttonAdvertising;
    private Button buttonInit;
    private Button buttonIssueOwnershipUTicket;
    private Button buttonApplyOwnershipUTicket;
    private Button buttonGetData;
    private Button buttonApplyConfigUTicket;
    private Button buttonApplyTallyUTicket;
    private Button buttonPermissionlessAdmin;
    private Button buttonShowRTickets;
    private Button buttonDisconnect;

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
        manufacturerController = new DeviceController(ThisDevice.USER_AGENT_OR_CLOUD_SERVER, "Manufacturer");
        manufacturerController.getExecutor()._executeOneTimeInitializeAgentOrServer();
//        manufacturerController.getNearbyManager().setMsgReceiver(manufacturerController.getMsgReceiver());

        textViewConnectingStatus = findViewById(R.id.textViewConnectingStatus);
        buttonScanManufacturer = findViewById(R.id.buttonScanManufacturer);
        buttonScan = findViewById(R.id.buttonScan);
        buttonAdvertising = findViewById(R.id.buttonAdvertising);
        buttonInit = findViewById(R.id.buttonInit);
        buttonIssueOwnershipUTicket = findViewById(R.id.buttonIssueOwnershipUTicket);
        buttonApplyOwnershipUTicket = findViewById(R.id.buttonApplyOwnershipUTicket);
        buttonGetData = findViewById(R.id.buttonGetData);
        buttonApplyConfigUTicket = findViewById(R.id.buttonApplyConfigUTicket);
        buttonApplyTallyUTicket = findViewById(R.id.buttonApplyTallyUTicket);
        buttonPermissionlessAdmin = findViewById(R.id.buttonPermissionlessAdmin);
        buttonShowRTickets = findViewById(R.id.buttonShowRTickets);
        buttonDisconnect = findViewById(R.id.buttonDisconnect);

        buttonInit.setEnabled(false);
        buttonIssueOwnershipUTicket.setEnabled(false);
        buttonApplyOwnershipUTicket.setEnabled(false);
        buttonGetData.setEnabled(false);
        buttonApplyConfigUTicket.setEnabled(false);
        buttonApplyTallyUTicket.setEnabled(false);
        buttonPermissionlessAdmin.setEnabled(false);
        buttonShowRTickets.setEnabled(false);

        deviceController.getNearbyViewModel().getIsConnected().observe(this, isConnected -> {
            SimpleLogger.simpleLog("info", "AdminAgentActivity: Admin Agent isConnected = " + isConnected);
            if (isConnected != null && isConnected) {
                textViewConnectingStatus.setText("Admin Agent connected to Voter Agent");
            } else {
                textViewConnectingStatus.setText("Not connected");
            }
        });

        buttonScanManufacturer.setOnClickListener(view -> {
            manufacturerController.connectToDevice("HC-04BLE",
                    () -> runOnUiThread(() -> {
                        Toast.makeText(AdminAgentActivity.this, "Manufacturer connected to VM", Toast.LENGTH_SHORT).show();
                        buttonInit.setEnabled(true);
                    }),
                    () -> runOnUiThread(() -> {
                        Toast.makeText(AdminAgentActivity.this, "Manufacturer disconnected", Toast.LENGTH_SHORT).show();
                        buttonInit.setEnabled(false);
                        buttonIssueOwnershipUTicket.setEnabled(false);
                        buttonApplyOwnershipUTicket.setEnabled(false);
                        buttonGetData.setEnabled(false);
                        buttonApplyConfigUTicket.setEnabled(false);
                        buttonApplyTallyUTicket.setEnabled(false);
                        buttonPermissionlessAdmin.setEnabled(false);
                    }),
                    textViewConnectingStatus
            );
        });

        // buttonScan: Admin agent connect to the voting machine
        buttonScan.setOnClickListener(view -> {
            deviceController.getNearbyViewModel().getNearbyManager(Environment.applicationContext,deviceController.getMsgReceiver()).stopAllActions();
            deviceController.connectToDevice("HC-04BLE",
                    () -> runOnUiThread(() -> {
                        Toast.makeText(AdminAgentActivity.this, "Admin Agent connected to VM", Toast.LENGTH_SHORT).show();
                        buttonInit.setEnabled(true);
                    }),
                    () -> runOnUiThread(() -> {
                        Toast.makeText(AdminAgentActivity.this, "Admin Agent disconnected", Toast.LENGTH_SHORT).show();
                        buttonInit.setEnabled(false);
                        buttonIssueOwnershipUTicket.setEnabled(false);
                        buttonApplyOwnershipUTicket.setEnabled(false);
                        buttonGetData.setEnabled(false);
                        buttonApplyConfigUTicket.setEnabled(false);
                        buttonApplyTallyUTicket.setEnabled(false);
                        buttonPermissionlessAdmin.setEnabled(false);
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

//                Map<String, String> arbitraryDict = new HashMap<>();
//                arbitraryDict.put("u_ticket_type", UTicket.TYPE_INITIALIZATION_UTICKET);
//                arbitraryDict.put("device_id", "no_id");
//                arbitraryDict.put("holder_id", deviceController.getSharedData().getThisPerson().getPersonPubKeyStr());
//                deviceController.getFlowIssuerIssueUTicket().issuerIssueUTicketToHerself("no_id", arbitraryDict);
//                deviceController.getFlowApplyUTicket().holderApplyUTicket("no_id");
                Map<String, String> arbitraryDict = new HashMap<>();
                arbitraryDict.put("u_ticket_type", UTicket.TYPE_INITIALIZATION_UTICKET);
                arbitraryDict.put("device_id", "no_id");
                arbitraryDict.put("holder_id", manufacturerController.getSharedData().getThisPerson().getPersonPubKeyStr());
                manufacturerController.getFlowIssuerIssueUTicket().issuerIssueUTicketToHerself("no_id", arbitraryDict);
                manufacturerController.getFlowApplyUTicket().holderApplyUTicket("no_id");

                while (!sendNextTicket) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                buttonIssueOwnershipUTicket.setEnabled(true);
            }
        });

        buttonIssueOwnershipUTicket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Map<String, String> generatedRequest = Map.of(
                        "device_id", connectedDeviceId,
                        "holder_id", deviceController.getSharedData().getThisPerson().getPersonPubKeyStr(),
                        "u_ticket_type", UTicket.TYPE_OWNERSHIP_UTICKET
                );
                try {
                    // [STAGE: (VL)]
                    if (manufacturerController.getFlowIssuerIssueUTicket().getSharedData().getDeviceTable().containsKey(AdminAgentActivity.connectedDeviceId)) {
                        String generatedUTicketJson = manufacturerController.getMsgGenerator().generateXxxUTicket(generatedRequest);
                        manufacturerController.getGeneratedMsgStorer().storeGeneratedXxxUTicket(generatedUTicketJson);

                        UTicket receivedUTicket = UTicket.jsonStrToUTicket(generatedUTicketJson);
                        deviceController.getReceivedMsgStorer().storeReceivedXxxUTicket(receivedUTicket);
                        deviceController.getExecutor().executeUpdateTicketOrder("holderGenerateOrReceiveUTicket", receivedUTicket);
                    } else {
                        SimpleLogger.simpleLog("info", "FlowIssueUTicket.issuerIssueUTicketToHolder: Device not in device table");
                    }
                } catch (Exception e) { // pragma: no cover -> Shouldn't Reach Here
                    throw new RuntimeException("buttonIssueOwnershipUTicket", e);
                }

                buttonApplyOwnershipUTicket.setEnabled(true);
            }
        });

        buttonApplyOwnershipUTicket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendNextTicket = false;
                deviceController.getFlowApplyUTicket().holderApplyUTicket(connectedDeviceId);
                while (!sendNextTicket) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                buttonGetData.setEnabled(true);
                buttonApplyConfigUTicket.setEnabled(true);
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

        // buttonApplyConfigUTicket: Init the voting machine with the data
        buttonApplyConfigUTicket.setOnClickListener(new View.OnClickListener() {
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
                buttonPermissionlessAdmin.setEnabled(true);
                buttonApplyTallyUTicket.setEnabled(true);
            }
        });

        // buttonPermissionlessAdmin: Send a permissionless ticket to the voting machine
        buttonPermissionlessAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
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
                    // TODO: Intent
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
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

        // buttonDisconnect: Disconnect the Bluetooth and Nearby
        buttonDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                manufacturerController.getBleManager().disconnect();
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