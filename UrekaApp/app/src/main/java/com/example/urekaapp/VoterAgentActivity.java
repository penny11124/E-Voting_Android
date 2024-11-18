package com.example.urekaapp;

import android.os.Bundle;
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

import com.example.urekaapp.ble.BLEManager;
import com.example.urekaapp.ble.BLEPermissionHelper;
import com.example.urekaapp.ble.BLEViewModel;

import java.util.Map;

import ureka.framework.logic.DeviceController;
import ureka.framework.model.data_model.ThisDevice;
import ureka.framework.model.message_model.UTicket;
import ureka.framework.resource.crypto.SerializationUtil;

public class VoterAgentActivity extends AppCompatActivity {
    private DeviceController deviceController;

    private Button buttonScan;
    private Button buttonRequestUTicket;
    private Button buttonApplyUTicket;
    private Button buttonShowRTicket;

    private BLEViewModel bleViewModel;

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

        if (!BLEPermissionHelper.hasPermissions(this)) {
            BLEPermissionHelper.requestPermissions(this);
        }

        bleViewModel = new ViewModelProvider(this).get(BLEViewModel.class);

        deviceController = new DeviceController(ThisDevice.USER_AGENT_OR_CLOUD_SERVER, "User Agent");
        deviceController.getExecutor()._executeOneTimeInitializeAgentOrServer();

        buttonScan = findViewById(R.id.buttonScanDevice);
        buttonRequestUTicket = findViewById(R.id.buttonRequestUTicket);
        buttonApplyUTicket = findViewById(R.id.buttonApplyUTicket);
        buttonShowRTicket = findViewById(R.id.buttonShowRTicket);
        buttonRequestUTicket.setEnabled(false);
        buttonApplyUTicket.setEnabled(false);
        buttonShowRTicket.setEnabled(false);

        buttonScan.setOnClickListener(view -> {
            deviceController.connectToDevice("HC-04BLE",
                    () -> runOnUiThread(() -> {
                        Toast.makeText(VoterAgentActivity.this, "Device connected!", Toast.LENGTH_SHORT).show();
                        buttonRequestUTicket.setEnabled(true);
                    }),
                    () -> runOnUiThread(() -> {
                        Toast.makeText(VoterAgentActivity.this, "Device disconnected!", Toast.LENGTH_SHORT).show();
                        buttonRequestUTicket.setEnabled(false);
                    })
            );
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