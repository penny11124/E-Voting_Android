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

import java.security.KeyPair;
import java.security.SecureRandom;
import java.security.interfaces.ECPublicKey;

import ureka.framework.Environment;
import ureka.framework.resource.crypto.ECC;
import ureka.framework.resource.crypto.SerializationUtil;
//import ureka.framework.test.operation.Test01SuccessWhenInitializeAgentOrServer;
//import ureka.framework.test.operation.Test02SuccessWhenInitializeDevice;
//import ureka.framework.test.operation.Test03SuccessWhenTransferDeviceOwnership;
//import ureka.framework.test.operation.Test04SuccessWhenAccessDeviceByOwner;
//import ureka.framework.test.operation.Test05SuccessWhenAccessDeviceByOthers;
//import ureka.framework.test.operation.Test06SuccessWhenAccessDeviceByPrivateSession;
//import ureka.framework.test.operation.Test11FailWhenInitializeAgentOrServer;
//import ureka.framework.test.operation.Test12FailWhenInitializeDevice;
//import ureka.framework.test.operation.Test13FailWhenTransferDeviceOwnership;
//import ureka.framework.test.operation.Test14FailWhenAccessDeviceByOwner;
//import ureka.framework.test.operation.Test15FailWhenAccessDeviceByOthers;
//import ureka.framework.test.operation.Test16FailWhenAccessDeviceByPrivateSession;

public class MainActivity extends AppCompatActivity {
    private String mode = "RUN";

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

        KeyPair keyPair1, keyPair2, keyPair3;
        try {
            keyPair1 = ECC.generateKeyPair();
            keyPair2 = ECC.generateKeyPair();
            keyPair3 = ECC.generateKeyPair();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Button buttonAdminAgent = findViewById(R.id.buttonAdminAgent);
        buttonAdminAgent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AdminAgentActivity.class);
                intent.putExtra("mode", mode);
                intent.putExtra("key1", SerializationUtil.keyToStr(keyPair1.getPublic(), "eccPublicKey"));
                intent.putExtra("key2", SerializationUtil.keyToStr(keyPair2.getPublic(), "eccPublicKey"));
                intent.putExtra("key3", SerializationUtil.keyToStr(keyPair3.getPublic(), "eccPublicKey"));
                startActivity(intent);
            }
        });

        Button buttonVoterAgent1 = findViewById(R.id.buttonVoterAgent1);
        buttonVoterAgent1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, VoterAgentActivity.class);
                intent.putExtra("mode", mode);
                intent.putExtra("publicKey", SerializationUtil.keyToStr(keyPair1.getPublic(), "eccPublicKey"));
                intent.putExtra("privateKey", SerializationUtil.keyToStr(keyPair1.getPrivate(), "eccPrivateKey"));
                startActivity(intent);
            }
        });

        Button buttonVoterAgent2 = findViewById(R.id.buttonVoterAgent2);
        buttonVoterAgent2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, VoterAgentActivity.class);
                intent.putExtra("mode", mode);
                intent.putExtra("publicKey", SerializationUtil.keyToStr(keyPair2.getPublic(), "eccPublicKey"));
                intent.putExtra("privateKey", SerializationUtil.keyToStr(keyPair2.getPrivate(), "eccPrivateKey"));
                startActivity(intent);
            }
        });

        Button buttonVoterAgent3 = findViewById(R.id.buttonVoterAgent3);
        buttonVoterAgent3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, VoterAgentActivity.class);
                intent.putExtra("mode", mode);
                intent.putExtra("publicKey", SerializationUtil.keyToStr(keyPair3.getPublic(), "eccPublicKey"));
                intent.putExtra("privateKey", SerializationUtil.keyToStr(keyPair3.getPrivate(), "eccPrivateKey"));
                startActivity(intent);
            }
        });

        Button buttonTest = findViewById(R.id.buttonTest);
        buttonTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                mode = "TEST";
//                Toast.makeText(MainActivity.this, "MODE is set to TEST.", Toast.LENGTH_SHORT).show();
//
//                try {
//                    KeyPair keyPair = ECC.generateKeyPair();
//
//                    ECPublicKey ecPublicKey = (ECPublicKey) keyPair.getPublic();
//                    textTest.setText(SerializationUtil.keyToStr(ecPublicKey));
//
//                } catch (Exception e) {
//                    throw new RuntimeException(e);
//                }
                Intent intent = new Intent(MainActivity.this, TestActivity.class);
                startActivity(intent);
            }
        });
    }
}