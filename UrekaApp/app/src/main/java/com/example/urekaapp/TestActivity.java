package com.example.urekaapp;

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
                            Log.d("TestActivity", data);
                            textViewDataReceived.setText("Data received: " + data);
                        });
                    }
                });
            }
        });

        buttonSendData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = "{\"message_operation\":\"MESSAGE_VERIFY_AND_EXECUTE\",\"message_type\":\"UTICKET\",\"message_str\":\"{\\\"protocol_version\\\":\\\"UREKA-1.0\\\",\\\"u_ticket_id\\\":\\\"CI3T1XsBos4MEEFMLuK0Wy2/h+rzghjkXDH0AwxoRCA=\\\",\\\"u_ticket_type\\\":\\\"INITIALIZATION\\\",\\\"device_id\\\":\\\"no_id\\\",\\\"ticket_order\\\":0,\\\"holder_id\\\":\\\"Vp0YwOXAkVf7wNO5gqfVqveAwcNh7YY42Lzk/7kc7kg=-fb2eO4YWd0mhg4e3rGePzUjn4RBpgTm9EnIoCXBwamg=\\\"}\"}$";
                textViewDataSent.setText(message);
                bleManager.sendData(message);
            }
        });
    }
}