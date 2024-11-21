package com.example.urekaapp;

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
import androidx.lifecycle.ViewModelProvider;

import com.example.urekaapp.ble.BLEManager;
import com.example.urekaapp.ble.BLEViewModel;

import ureka.framework.Environment;
import ureka.framework.model.message_model.Message;
import ureka.framework.model.message_model.UTicket;
import ureka.framework.resource.logger.SimpleLogger;

public class TestActivity extends AppCompatActivity {
    private BLEManager bleManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        Environment.applicationContext = TestActivity.this;
        setContentView(R.layout.activity_test);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        BLEViewModel bleViewModel = new ViewModelProvider(this).get(BLEViewModel.class);
        bleManager = bleViewModel.getBLEManager(TestActivity.this);

        Button buttonConnect = findViewById(R.id.buttonConnect);
        Button buttonSendData = findViewById(R.id.buttonSendData);
        TextView textViewConnectingStatus = findViewById(R.id.textViewConnectionStatus);
        TextView textViewDataSent = findViewById(R.id.textViewDataSent);
        TextView textViewDataReceived = findViewById(R.id.textViewDataReceived);

        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(TestActivity.this, "Connecting...", Toast.LENGTH_SHORT).show();
                bleManager.startScan("HC-04BLE", new BLEManager.BLECallback() {
                    @Override
                    public void onConnected() {
                        SimpleLogger.simpleLog("info", "Device connected!");
                        runOnUiThread(() -> textViewConnectingStatus.setText("Device Connected."));
                    }

                    @Override
                    public void onDisconnected() {
                        SimpleLogger.simpleLog("info", "Device disconnected!");
                        runOnUiThread(() -> textViewConnectingStatus.setText("Device Disconnected."));
                    }

                    @Override
                    public void onDataReceived(String data) {
                        SimpleLogger.simpleLog("info", "Received data: " + data);
                        runOnUiThread(() -> {
                            textViewDataReceived.setText("Data received: " + data);
                        });
                    }
                });
            }
        });

        buttonSendData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StringBuilder message = new StringBuilder();
                for (int i = 0; i < 26; i++) {
                    char c = (char) ('A' + i);
                    for (int j = 0; j < i; j++) {
                        message.append(c);
                    }
                }
                message.append("$");
                textViewDataSent.setText("Data sent.");
                bleManager.sendData(message.toString());
            }
        });
    }
}