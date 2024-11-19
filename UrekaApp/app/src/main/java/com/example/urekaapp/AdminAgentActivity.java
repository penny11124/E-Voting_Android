package com.example.urekaapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.checkerframework.checker.units.qual.A;
import org.junit.platform.commons.util.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import ureka.framework.logic.DeviceController;
import ureka.framework.model.data_model.ThisDevice;
import ureka.framework.model.message_model.UTicket;
import ureka.framework.resource.crypto.SerializationUtil;

public class AdminAgentActivity extends AppCompatActivity {
    private DeviceController deviceController;

    private ArrayList<String> candidates; // The names of the candidates
    private ArrayList<Integer> candidateVotes; // The votes od the candidates
    private ArrayList<String> voters; // The public key of the voters
    private ArrayList<Boolean> voterVoted; // Whether the voters had voted
    private String connectedDeviceId; // The deviceId of the voting machine

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

        // components initialization
        Button buttonInit = findViewById(R.id.buttonInit);
        Button buttonGetData = findViewById(R.id.buttonGetData);
        Button buttonApplyInitUTicket = findViewById(R.id.buttonApplyInitUTicket);
        Button buttonApplyTallyUTicket = findViewById(R.id.buttonApplyTallyUTicket);
        Button buttonShowRTickets = findViewById(R.id.buttonShowRTickets);

        String mode = getIntent().getStringExtra("mode");
        if (!Objects.equals(mode, "TEST")) {
            buttonInit.setEnabled(false);
            buttonGetData.setEnabled(false);
            buttonApplyInitUTicket.setEnabled(false);
            buttonApplyTallyUTicket.setEnabled(false);
            buttonShowRTickets.setEnabled(false);
        }

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
                arbitraryDict.put("uTicketType", UTicket.TYPE_INITIALIZATION_UTICKET);
                arbitraryDict.put("deviceId", "noId");
                arbitraryDict.put("holderId", deviceController.getSharedData().getThisPerson().getPersonPubKeyStr());
                deviceController.getFlowIssuerIssueUTicket().issuerIssueUTicketToHerself("noId", arbitraryDict);
                deviceController.getFlowApplyUTicket().holderApplyUTicket("noId");
                // this.iotDevice.getMsgReceiver()._recvXxxMessage();
                // deviceController.getMsgReceiver()._recvXxxMessage();
                for (String key : deviceController.getFlowApplyUTicket().getReceivedMsgStorer().getSharedData().getDeviceTable().keySet()) {
                    connectedDeviceId = key;
                }
            }
        });

        // buttonGetData: Get the data from the blockchain(simulated by giving hard-coded data)
        buttonGetData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                candidates = new ArrayList<>();
                candidates.add("Alice");
                candidates.add("Bob");
                candidates.add("Carol");

                voters = new ArrayList<>();
                voters.add("G27PEAvPwj985TT9kWYJ1Z+3vYezpNts0hz5shKLPTY=-pxGy2l6X9NWbnqxYAufqj9crC+fig8XJcrqOLxYTJbQ=");
                voters.add("bQuwtyMQ/0D1dWGPPlP7A38Ua3MbRKS96Fh9d8oanH0=-Gp7tEaPD1mrFtkt8wMNgOKkxehARmprzUhhmZm2d864=");
            }
        });

        // buttonApplyInitUTicket: Init the voting machine with the data
        buttonApplyInitUTicket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String generatedTaskScope = SerializationUtil.dictToJsonStr(Map.of("ALL", "allow"));
                Map<String, String> generatedRequest = Map.of(
                        "deviceId", connectedDeviceId,
                        "holderId", deviceController.getSharedData().getThisPerson().getPersonPubKeyStr(),
                        "uTicketType", UTicket.TYPE_SELFACCESS_UTICKET,
                        "taskScope", generatedTaskScope
                );
                deviceController.getFlowIssuerIssueUTicket().issuerIssueUTicketToHerself(connectedDeviceId, generatedRequest);

                // Apply UTicket + CRKE
                String generatedCommand = "HELLO-1";
                deviceController.getFlowApplyUTicket().holderApplyUTicket(connectedDeviceId, generatedCommand);
//                this.iotDevice.getMsgReceiver()._recvXxxMessage();
//                this.userAgentDO.getMsgReceiver()._recvXxxMessage();
//                this.iotDevice.getMsgReceiver()._recvXxxMessage();
//                this.userAgentDO.getMsgReceiver()._recvXxxMessage();

                // Send UToken: candidates and voters
                for (String candidate: candidates) {
                    String data = "C:" + candidate;
                    deviceController.getFlowIssueUToken().holderSendCmd(connectedDeviceId, data, false);
//                    this.iotDevice.getMsgReceiver()._recvXxxMessage();
//                    this.userAgentDO.getMsgReceiver()._recvXxxMessage();
                }
                for (String voter: voters) {
                    String data = "C-" + voter;
                    deviceController.getFlowIssueUToken().holderSendCmd(connectedDeviceId, data, false);
//                    this.iotDevice.getMsgReceiver()._recvXxxMessage();
//                    this.userAgentDO.getMsgReceiver()._recvXxxMessage();
                }

                // Access End + receive RTicket
                deviceController.getFlowIssueUToken().holderSendCmd(connectedDeviceId, "ACCESS_END", true);
//                this.iotDevice.getMsgReceiver()._recvXxxMessage();
//                this.userAgentDO.getMsgReceiver()._recvXxxMessage();
            }
        });

        // buttonApplyTallyUTicket: Tally the votes
        buttonApplyTallyUTicket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String generatedTaskScope = SerializationUtil.dictToJsonStr(Map.of("ALL", "allow"));
                Map<String, String> generatedRequest = Map.of(
                        "deviceId", connectedDeviceId,
                        "holderId", deviceController.getSharedData().getThisPerson().getPersonPubKeyStr(),
                        "uTicketType", UTicket.TYPE_SELFACCESS_UTICKET,
                        "taskScope", generatedTaskScope
                );
                deviceController.getFlowIssuerIssueUTicket().issuerIssueUTicketToHerself(connectedDeviceId,generatedRequest);

                // Apply UTicket + CRKE
                String generatedCommand = "HELLO-1";
                deviceController.getFlowApplyUTicket().holderApplyUTicket(connectedDeviceId,generatedCommand);
//                this.iotDevice.getMsgReceiver()._recvXxxMessage();
//                this.userAgentDO.getMsgReceiver()._recvXxxMessage();
//                this.iotDevice.getMsgReceiver()._recvXxxMessage();
//                this.userAgentDO.getMsgReceiver()._recvXxxMessage();

                // Send UToken: the votes of candidates
                String data = "TC";
                deviceController.getFlowIssueUToken().holderSendCmd(connectedDeviceId, data, false);
//                this.iotDevice.getMsgReceiver()._recvXxxMessage();
//                this.userAgentDO.getMsgReceiver()._recvXxxMessage();
                data = deviceController.getFlowIssueUToken().getExecutor().getSharedData().getCurrentSession().getPlaintextData();

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
                    deviceController.getFlowIssueUToken().holderSendCmd(connectedDeviceId, data, false);
//                    this.iotDevice.getMsgReceiver()._recvXxxMessage();
//                    this.userAgentDO.getMsgReceiver()._recvXxxMessage();
                    data = deviceController.getFlowIssueUToken().getExecutor().getSharedData().getCurrentSession().getPlaintextData();

                    if (Objects.equals(data, "---")) {
                        break;
                    }

                    result = data.split(":");
                    if (!Objects.equals(voters.get(i), result[0])) {
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
                deviceController.getFlowIssueUToken().holderSendCmd(connectedDeviceId, "ACCESS_END", true);
//                this.iotDevice.getMsgReceiver()._recvXxxMessage();
//                this.userAgentDO.getMsgReceiver()._recvXxxMessage();
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
    }
}