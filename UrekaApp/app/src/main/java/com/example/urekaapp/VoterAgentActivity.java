package com.example.urekaapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Map;

import ureka.framework.logic.DeviceController;
import ureka.framework.model.data_model.ThisDevice;
import ureka.framework.model.message_model.UTicket;
import ureka.framework.resource.crypto.SerializationUtil;

public class VoterAgentActivity extends AppCompatActivity {
    private DeviceController deviceController;

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

        Button buttonRequestUTicket = findViewById(R.id.buttonRequestUTicket);
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

        Button buttonApplyUTicket = findViewById(R.id.buttonApplyUTicket);
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

        Button buttonShowRTicket = findViewById(R.id.buttonShowRTicket);
        buttonShowRTicket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO
            }
        });
    }
}