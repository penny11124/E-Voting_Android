package com.example.urekaapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ureka.framework.Environment;
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

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Environment.initialize(this);
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
                startActivity(intent);
                finish();
            }
        });

        Button buttonVoterAgent = findViewById(R.id.buttonVoterAgent);
        buttonVoterAgent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, VoterAgentActivity.class);
                startActivity(intent);
                finish();
            }
        });

        Button buttonVotingMachine = findViewById(R.id.buttonVotingMachine);
        buttonVotingMachine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, VotingMachineActivity.class);
                startActivity(intent);
                finish();
            }
        });

        Button buttonTest = findViewById(R.id.buttonTest);
        buttonTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                try {
//                    KeyPair keyPair = ECC.generateKeyPair();
//                    ECPublicKey ecPublicKey = (ECPublicKey) keyPair.getPublic();
//                    ECPrivateKey ecPrivateKey = (ECPrivateKey) keyPair.getPrivate();
//                    System.out.println("ECPublicKey = " + ecPublicKey);
//                    System.out.println("ECPrivateKey = " + ecPrivateKey);
//                } catch (Exception e) {
//                    throw new RuntimeException(e);
//                }

                // Create an ExecutorService to run the tasks
                ExecutorService executorService = Executors.newSingleThreadExecutor();

                // Add heavy test tasks in sequence
                executorService.execute(() -> {
                    Test01SuccessWhenInitializeAgentOrServer test01SuccessWhenInitializeAgentOrServer = new Test01SuccessWhenInitializeAgentOrServer();
                    test01SuccessWhenInitializeAgentOrServer.runAll();
                    showText(textTest, "Test01 completed successfully");
                    // showToast("Test01 completed successfully");

                    Test02SuccessWhenInitializeDevice test02SuccessWhenInitializeDevice = new Test02SuccessWhenInitializeDevice();
                    test02SuccessWhenInitializeDevice.runAll();
                    showText(textTest, "Test02 completed successfully");
                    // showToast("Test02 completed successfully");

                    Test03SuccessWhenTransferDeviceOwnership test03SuccessWhenTransferDeviceOwnership = new Test03SuccessWhenTransferDeviceOwnership();
                    test03SuccessWhenTransferDeviceOwnership.runAll();
                    showText(textTest, "Test03 completed successfully");
                    // showToast("Test03 completed successfully");

                    Test04SuccessWhenAccessDeviceByOwner test04SuccessWhenAccessDeviceByOwner = new Test04SuccessWhenAccessDeviceByOwner();
                    test04SuccessWhenAccessDeviceByOwner.runAll();
                    showText(textTest, "Test04 completed successfully");
                    // showToast("Test04 completed successfully");

                    Test05SuccessWhenAccessDeviceByOthers test05SuccessWhenAccessDeviceByOthers = new Test05SuccessWhenAccessDeviceByOthers();
                    test05SuccessWhenAccessDeviceByOthers.runAll();
                    showText(textTest, "Test05 completed successfully");
                    // showToast("Test05 completed successfully");

                    Test06SuccessWhenAccessDeviceByPrivateSession test06SuccessWhenAccessDeviceByPrivateSession = new Test06SuccessWhenAccessDeviceByPrivateSession();
                    test06SuccessWhenAccessDeviceByPrivateSession.runAll();
                    showText(textTest, "Test06 completed successfully");
                    // showToast("Test06 completed successfully");

                    Test11FailWhenInitializeAgentOrServer test11FailWhenInitializeAgentOrServer = new Test11FailWhenInitializeAgentOrServer();
                    test11FailWhenInitializeAgentOrServer.runAll();
                    showText(textTest, "Test11 completed successfully");
                    // showToast("Test11 completed successfully");

                    Test12FailWhenInitializeDevice test12FailWhenInitializeDevice = new Test12FailWhenInitializeDevice();
                    test12FailWhenInitializeDevice.runAll();
                    showText(textTest, "Test12 completed successfully");
                    // showToast("Test12 completed successfully");

                    Test13FailWhenTransferDeviceOwnership test13FailWhenTransferDeviceOwnership = new Test13FailWhenTransferDeviceOwnership();
                    test13FailWhenTransferDeviceOwnership.runAll();
                    showText(textTest, "Test13 completed successfully");
                    // showToast("Test13 completed successfully");

                    Test14FailWhenAccessDeviceByOwner test14FailWhenAccessDeviceByOwner = new Test14FailWhenAccessDeviceByOwner();
                    test14FailWhenAccessDeviceByOwner.runAll();
                    showText(textTest, "Test14 completed successfully");
                    // showToast("Test14 completed successfully");

                    Test15FailWhenAccessDeviceByOthers test15FailWhenAccessDeviceByOthers = new Test15FailWhenAccessDeviceByOthers();
                    test15FailWhenAccessDeviceByOthers.runAll();
                    showText(textTest, "Test15 completed successfully");
                    // showToast("Test15 completed successfully");

                    Test16FailWhenAccessDeviceByPrivateSession test16FailWhenAccessDeviceByPrivateSession = new Test16FailWhenAccessDeviceByPrivateSession();
                    test16FailWhenAccessDeviceByPrivateSession.runAll();
                    showText(textTest, "Test16 completed successfully");
                    // showToast("Test16 completed successfully");

                    showToast("All Tests are completed.");

                    // Shutdown the executor when all tasks are finished
                    executorService.shutdown();
                });
            }
        });
    }

    // Method to show a Toast message on the UI thread
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private void showToast(final String message) {
        mainHandler.post(() -> Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show());
    }
    private void showText(final TextView textView, final String message) {
        mainHandler.post(() -> textView.setText(message));
    }
}