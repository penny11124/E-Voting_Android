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

import java.io.Serial;
import java.security.KeyPair;
import java.security.SecureRandom;
import java.security.interfaces.ECPublicKey;

import ureka.framework.Environment;
import ureka.framework.resource.crypto.ECC;
import ureka.framework.resource.crypto.SerializationUtil;
import ureka.framework.resource.logger.SimpleLogger;

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

        // Fixed generated data
        String publicKey1 = "VRWuwWlGZ3uyPfVuBpnKi20pK+WG89ePRp46zwbnpQ0=-/qUcFmCE0CuGCXdLD0w9NEzh7CRr8lQDDuGtZOIBp2Q=";
        String privateKey1 = "MIGNAgEAMBAGByqGSM49AgEGBSuBBAAKBHYwdAIBAQQgbjndhYEkOgKL1nGW2AWaDuFeB9GglqOIzcR414wd4jWgBwYFK4EEAAqhRANCAARVFa7BaUZne7I99W4GmcqLbSkr5Ybz149GnjrPBuelDf6lHBZghNArhgl3Sw9MPTRM4ewka/JUAw7hrWTiAadk";
        String publicKey2 = "b6U7if/Qs+ph/V7qHC2spvVq8/oDZzHQ+XjgtSDuWUk=-g4XoiKDF/wV94Rpz9OVoKxyHAcugjcRSroJgBkEwdnY=";
        String privateKey2 = "MIGNAgEAMBAGByqGSM49AgEGBSuBBAAKBHYwdAIBAQQgtuM6xlRey5BxPSmNUY4fdVzGdUZdR2GDWFhT4TCS/FagBwYFK4EEAAqhRANCAARvpTuJ/9Cz6mH9XuocLaym9Wrz+gNnMdD5eOC1IO5ZSYOF6Iigxf8FfeEac/TlaCschwHLoI3EUq6CYAZBMHZ2";

        Button buttonAdminAgent = findViewById(R.id.buttonAdminAgent);
        buttonAdminAgent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AdminAgentActivity.class);
                intent.putExtra("key1", publicKey1);
                intent.putExtra("key2", publicKey2);
                startActivity(intent);
            }
        });

        Button buttonVoterAgent1 = findViewById(R.id.buttonVoterAgent1);
        buttonVoterAgent1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, VoterAgentActivity.class);
                intent.putExtra("publicKey", publicKey1);
                intent.putExtra("privateKey", privateKey1);
                startActivity(intent);
            }
        });

        Button buttonVoterAgent2 = findViewById(R.id.buttonVoterAgent2);
        buttonVoterAgent2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, VoterAgentActivity.class);
                intent.putExtra("publicKey", publicKey2);
                intent.putExtra("privateKey", privateKey2);
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