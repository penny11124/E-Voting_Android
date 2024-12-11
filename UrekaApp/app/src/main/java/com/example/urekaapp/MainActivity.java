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

        KeyPair keyPair1, keyPair2;
        try {
            keyPair1 = ECC.generateKeyPair();
            keyPair2 = ECC.generateKeyPair();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Button buttonAdminAgent = findViewById(R.id.buttonAdminAgent);
        buttonAdminAgent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AdminAgentActivity.class);
                intent.putExtra("key1", SerializationUtil.publicKeyToBase64(keyPair1.getPublic()));
                intent.putExtra("key2", SerializationUtil.publicKeyToBase64(keyPair2.getPublic()));
                startActivity(intent);
            }
        });

        Button buttonVoterAgent1 = findViewById(R.id.buttonVoterAgent1);
        buttonVoterAgent1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, VoterAgentActivity.class);
                intent.putExtra("publicKey", SerializationUtil.publicKeyToBase64(keyPair1.getPublic()));
                intent.putExtra("privateKey", SerializationUtil.privateKeyToBase64(keyPair1.getPrivate()));
                startActivity(intent);
            }
        });

        Button buttonVoterAgent2 = findViewById(R.id.buttonVoterAgent2);
        buttonVoterAgent2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, VoterAgentActivity.class);
                intent.putExtra("publicKey", SerializationUtil.publicKeyToBase64(keyPair2.getPublic()));
                intent.putExtra("privateKey", SerializationUtil.privateKeyToBase64(keyPair2.getPrivate()));
                startActivity(intent);
            }
        });

        Button buttonTest = findViewById(R.id.buttonTest);
        buttonTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TestActivity.class);
                startActivity(intent);
            }
        });
    }
}