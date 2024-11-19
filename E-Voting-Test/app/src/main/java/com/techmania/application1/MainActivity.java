package com.techmania.application1;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;

import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    ConnectionsClient connectionsClient;

    Button advertiseBtn;
    Button discoverBtn;
    Button disconnectBtn;
    Button sendJsonBtn;
    TextView receivedJsonTextView;

    private Set<String> connectedEndpoints = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connectionsClient = Nearby.getConnectionsClient(this);

        advertiseBtn = findViewById(R.id.advertiseButton);
        discoverBtn = findViewById(R.id.discoverButton);
        disconnectBtn = findViewById(R.id.disconnectButton);
        sendJsonBtn = findViewById(R.id.sendJsonButton);
        receivedJsonTextView = findViewById(R.id.receivedJsonTextView);

        advertiseBtn.setOnClickListener(v -> startAdvertising());
        discoverBtn.setOnClickListener(v -> startDiscovery());
        disconnectBtn.setOnClickListener(v -> disconnectFromAllEndpoints());

        sendJsonBtn.setOnClickListener(v -> {
            if (!connectedEndpoints.isEmpty()) {
                String endpointId = connectedEndpoints.iterator().next();
                String jsonToSend = "{\"message\":\"Hello, Device! This is a test JSON.\"}";
                sendJson(endpointId, jsonToSend);
            } else {
                Log.d("Nearby", "No connected endpoints to send JSON");
                Toast.makeText(this, "No connected devices", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void startAdvertising() {
        connectionsClient.startAdvertising(
                        "TestDevice1",
                        getPackageName(),  // Service ID
                        connectionLifecycleCallback,
                        new AdvertisingOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build()  // P2P
                ).addOnSuccessListener(unused -> Log.d("Nearby", "Advertising started"))
                .addOnFailureListener(e -> Log.d("Nearby", "Advertising failed", e));
    }

    public void startDiscovery() {
        connectionsClient.startDiscovery(
                        getPackageName(),
                        endpointDiscoveryCallback,
                        new DiscoveryOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build()  // 設定 P2P 策略
                ).addOnSuccessListener(unused -> Log.d("Nearby", "Discovery started"))
                .addOnFailureListener(e -> Log.d("Nearby", "Discovery failed", e));
    }



    ConnectionLifecycleCallback connectionLifecycleCallback = new ConnectionLifecycleCallback() {
        @Override
        public void onConnectionInitiated(String endpointId, ConnectionInfo info) {
            Log.d("Nearby", "Connection initiated with: " + endpointId);
            connectionsClient.acceptConnection(endpointId, payloadCallback);
        }

        @Override
        public void onConnectionResult(String endpointId, ConnectionResolution result) {
            if (result.getStatus().isSuccess()) {
                Log.d("Nearby", "Successfully connected to: " + endpointId);
                connectedEndpoints.add(endpointId);
            } else {
                Log.d("Nearby", "Connection failed with: " + endpointId);
            }
        }

        @Override
        public void onDisconnected(String endpointId) {
            Log.d("Nearby", "Disconnected from: " + endpointId);
            connectedEndpoints.remove(endpointId);
        }
    };

    public void sendJson(String endpointId, String jsonToSend) {
        byte[] jsonBytes = jsonToSend.getBytes();
        Payload jsonPayload = Payload.fromBytes(jsonBytes);
        connectionsClient.sendPayload(endpointId, jsonPayload)
                .addOnSuccessListener(unused -> Log.d("Nearby", "JSON sent"))
                .addOnFailureListener(e -> Log.d("Nearby", "Failed to send JSON", e));
    }

    PayloadCallback payloadCallback = new PayloadCallback() {
        @Override
        public void onPayloadReceived(String endpointId, Payload payload) {
            byte[] receivedBytes = payload.asBytes();
            if (receivedBytes != null) {
                String receivedJson = new String(receivedBytes);
                Log.d("Nearby", "Received JSON: " + receivedJson);

                receivedJsonTextView.setText(receivedJson);
            }
        }

        @Override
        public void onPayloadTransferUpdate(String endpointId, PayloadTransferUpdate update) {
            // 處理傳輸更新（例如顯示傳輸進度）
        }
    };

    public void disconnectFromAllEndpoints() {
        for (String endpointId : connectedEndpoints) {
            connectionsClient.disconnectFromEndpoint(endpointId);
            Log.d("Nearby", "Disconnected from endpoint: " + endpointId);
        }

        connectedEndpoints.clear();

        Log.d("Nearby", "All endpoints disconnected");
        Toast.makeText(this, "Disconnected from all devices", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 停止廣播和發現，釋放資源
        connectionsClient.stopAdvertising();
        connectionsClient.stopDiscovery();
    }


}