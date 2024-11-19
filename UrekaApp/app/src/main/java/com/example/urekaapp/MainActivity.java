package com.example.urekaapp;

import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.interfaces.ECPublicKey;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ureka.framework.model.message_model.UTicket;
import ureka.framework.resource.crypto.ECC;
import ureka.framework.resource.crypto.ECDH;
import ureka.framework.resource.crypto.SerializationUtil;
import ureka.framework.resource.logger.SimpleLogger;
import ureka.framework.test.operation.Test01SuccessWhenInitializeAgentOrServer;
import ureka.framework.test.operation.Test02SuccessWhenInitializeDevice;
import ureka.framework.test.operation.Test03SuccessWhenTransferDeviceOwnership;
import ureka.framework.test.operation.Test04SuccessWhenAccessDeviceByOwner;
import ureka.framework.test.operation.Test05SuccessWhenAccessDeviceByOthers;
import ureka.framework.test.operation.Test06SuccessWhenAccessDeviceByPrivateSession;
import ureka.framework.test.operation.Test11FailWhenInitializeAgentOrServer;
import ureka.framework.test.operation.Test12FailWhenInitializeDevice;
import ureka.framework.test.operation.Test13FailWhenTransferDeviceOwnership;
import ureka.framework.test.operation.Test14FailWhenAccessDeviceByOwner;
import ureka.framework.test.operation.Test15FailWhenAccessDeviceByOthers;
import ureka.framework.test.operation.Test16FailWhenAccessDeviceByPrivateSession;

import android_serialport_api.SerialPort;

public class MainActivity extends AppCompatActivity {
    private String mode = "RUN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView textTest = findViewById(R.id.textTest);

        Button buttonAdminAgent = findViewById(R.id.buttonAdminAgent);
        buttonAdminAgent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AdminAgentActivity.class);
                intent.putExtra("mode", mode);
                startActivity(intent);
                finish();
            }
        });

        Button buttonVoterAgent = findViewById(R.id.buttonVoterAgent);
        buttonVoterAgent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, VoterAgentActivity.class);
                intent.putExtra("mode", mode);
                startActivity(intent);
                finish();
            }
        });

        Button buttonVotingMachine = findViewById(R.id.buttonVotingMachine);
        buttonVotingMachine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, VotingMachineActivity.class);
                intent.putExtra("mode", mode);
                startActivity(intent);
                finish();
            }
        });

        Button buttonTest = findViewById(R.id.buttonTest);
        buttonTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mode = "TEST";
                Toast.makeText(MainActivity.this, "MODE is set to TEST.", Toast.LENGTH_SHORT).show();

                try {
                    KeyPair keyPair = ECC.generateKeyPair();

                    UTicket newUTicket = new UTicket();
                    newUTicket.setUTicketType(UTicket.TYPE_INITIALIZATION_UTICKET);
                    newUTicket.setDeviceId("noId");
                    newUTicket.setHolderId(SerializationUtil.keyToStr(keyPair.getPublic()));
                    newUTicket.setTicketOrder(0);
                    newUTicket.setUTicketId(ECDH.generateSha256HashStr(UTicket.uTicketToJsonStr(newUTicket)));
                    SimpleLogger.simpleLog("info", "UTicket: " + UTicket.uTicketToJsonStr(newUTicket));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                
//                // Create an ExecutorService to run the tasks
//                ExecutorService executorService = Executors.newSingleThreadExecutor();
//
//                // Add heavy test tasks in sequence
//                executorService.execute(() -> {
//                    Test01SuccessWhenInitializeAgentOrServer test01SuccessWhenInitializeAgentOrServer = new Test01SuccessWhenInitializeAgentOrServer();
//                    test01SuccessWhenInitializeAgentOrServer.runAll();
//                    showText(textTest, "Test01 completed successfully");
//                    // showToast("Test01 completed successfully");
//
//                    Test02SuccessWhenInitializeDevice test02SuccessWhenInitializeDevice = new Test02SuccessWhenInitializeDevice();
//                    test02SuccessWhenInitializeDevice.runAll();
//                    showText(textTest, "Test02 completed successfully");
//                    // showToast("Test02 completed successfully");
//
//                    Test03SuccessWhenTransferDeviceOwnership test03SuccessWhenTransferDeviceOwnership = new Test03SuccessWhenTransferDeviceOwnership();
//                    test03SuccessWhenTransferDeviceOwnership.runAll();
//                    showText(textTest, "Test03 completed successfully");
//                    // showToast("Test03 completed successfully");
//
//                    Test04SuccessWhenAccessDeviceByOwner test04SuccessWhenAccessDeviceByOwner = new Test04SuccessWhenAccessDeviceByOwner();
//                    test04SuccessWhenAccessDeviceByOwner.runAll();
//                    showText(textTest, "Test04 completed successfully");
//                    // showToast("Test04 completed successfully");
//
//                    Test05SuccessWhenAccessDeviceByOthers test05SuccessWhenAccessDeviceByOthers = new Test05SuccessWhenAccessDeviceByOthers();
//                    test05SuccessWhenAccessDeviceByOthers.runAll();
//                    showText(textTest, "Test05 completed successfully");
//                    // showToast("Test05 completed successfully");
//
//                    Test06SuccessWhenAccessDeviceByPrivateSession test06SuccessWhenAccessDeviceByPrivateSession = new Test06SuccessWhenAccessDeviceByPrivateSession();
//                    test06SuccessWhenAccessDeviceByPrivateSession.runAll();
//                    showText(textTest, "Test06 completed successfully");
//                    // showToast("Test06 completed successfully");
//
//                    Test11FailWhenInitializeAgentOrServer test11FailWhenInitializeAgentOrServer = new Test11FailWhenInitializeAgentOrServer();
//                    test11FailWhenInitializeAgentOrServer.runAll();
//                    showText(textTest, "Test11 completed successfully");
//                    // showToast("Test11 completed successfully");
//
//                    Test12FailWhenInitializeDevice test12FailWhenInitializeDevice = new Test12FailWhenInitializeDevice();
//                    test12FailWhenInitializeDevice.runAll();
//                    showText(textTest, "Test12 completed successfully");
//                    // showToast("Test12 completed successfully");
//
//                    Test13FailWhenTransferDeviceOwnership test13FailWhenTransferDeviceOwnership = new Test13FailWhenTransferDeviceOwnership();
//                    test13FailWhenTransferDeviceOwnership.runAll();
//                    showText(textTest, "Test13 completed successfully");
//                    // showToast("Test13 completed successfully");
//
//                    Test14FailWhenAccessDeviceByOwner test14FailWhenAccessDeviceByOwner = new Test14FailWhenAccessDeviceByOwner();
//                    test14FailWhenAccessDeviceByOwner.runAll();
//                    showText(textTest, "Test14 completed successfully");
//                    // showToast("Test14 completed successfully");
//
//                    Test15FailWhenAccessDeviceByOthers test15FailWhenAccessDeviceByOthers = new Test15FailWhenAccessDeviceByOthers();
//                    test15FailWhenAccessDeviceByOthers.runAll();
//                    showText(textTest, "Test15 completed successfully");
//                    // showToast("Test15 completed successfully");
//
//                    Test16FailWhenAccessDeviceByPrivateSession test16FailWhenAccessDeviceByPrivateSession = new Test16FailWhenAccessDeviceByPrivateSession();
//                    test16FailWhenAccessDeviceByPrivateSession.runAll();
//                    showText(textTest, "Test16 completed successfully");
//                    // showToast("Test16 completed successfully");
//
//                    showFinishToast();
//
//                    // Shutdown the executor when all tasks are finished
//                    executorService.shutdown();
//                });
            }
        });
    }

    // Method to show a Toast message on the UI thread
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private void showFinishToast() {
        mainHandler.post(() -> Toast.makeText(MainActivity.this, "All Tests are completed.", Toast.LENGTH_SHORT).show());
    }
    private void showText(final TextView textView, final String message) {
        mainHandler.post(() -> textView.setText(message));
    }
}