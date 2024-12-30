package com.example.urekaapp;

import android.app.ServiceStartNotAllowedException;
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
import java.io.Serializable;
import java.security.KeyPair;
import java.security.SecureRandom;
import java.security.interfaces.ECPublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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

//        try {
//            KeyPair keyPair = ECC.generateKeyPair();
//            SimpleLogger.simpleLog("info", "publicKey = " + SerializationUtil.publicKeyToBase64(keyPair.getPublic()));
//            SimpleLogger.simpleLog("info", "privateKey = " + SerializationUtil.privateKeyToBase64(keyPair.getPrivate()));
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }

        // Fixed generated data
        String publicKey1 = "VRWuwWlGZ3uyPfVuBpnKi20pK+WG89ePRp46zwbnpQ0=-/qUcFmCE0CuGCXdLD0w9NEzh7CRr8lQDDuGtZOIBp2Q=";
        String privateKey1 = "MIGNAgEAMBAGByqGSM49AgEGBSuBBAAKBHYwdAIBAQQgbjndhYEkOgKL1nGW2AWaDuFeB9GglqOIzcR414wd4jWgBwYFK4EEAAqhRANCAARVFa7BaUZne7I99W4GmcqLbSkr5Ybz149GnjrPBuelDf6lHBZghNArhgl3Sw9MPTRM4ewka/JUAw7hrWTiAadk";
        String publicKey2 = "b6U7if/Qs+ph/V7qHC2spvVq8/oDZzHQ+XjgtSDuWUk=-g4XoiKDF/wV94Rpz9OVoKxyHAcugjcRSroJgBkEwdnY=";
        String privateKey2 = "MIGNAgEAMBAGByqGSM49AgEGBSuBBAAKBHYwdAIBAQQgtuM6xlRey5BxPSmNUY4fdVzGdUZdR2GDWFhT4TCS/FagBwYFK4EEAAqhRANCAARvpTuJ/9Cz6mH9XuocLaym9Wrz+gNnMdD5eOC1IO5ZSYOF6Iigxf8FfeEac/TlaCschwHLoI3EUq6CYAZBMHZ2";
        String publicKey3 = "GendgRczWi+MDpzS8TX7nTbW+nHhdetpowALVyTZcwA=-bOVeGXtQZtG2QFsMyzOK0+BUh2J8qPc2BmirJ9+91Eo=";
        String privateKey3 = "MIGNAgEAMBAGByqGSM49AgEGBSuBBAAKBHYwdAIBAQQgXop2IEIeHjn58jbprI0/2HuOcSOyFQA66Ly2z5h/RUSgBwYFK4EEAAqhRANCAAQZ6d2BFzNaL4wOnNLxNfudNtb6ceF162mjAAtXJNlzAGzlXhl7UGbRtkBbDMszitPgVIdifKj3NgZoqyffvdRK";
        String publicKey4 = "okY9cnzWav1F4eSJqmbZvr4n23UcCv0Sqh+BBYMi/Ok=-mhlnzAubhw1NhZRFAmYIjWdD92AjeNgM2kVz3ktGHK8=";
        String privateKey4 = "MIGNAgEAMBAGByqGSM49AgEGBSuBBAAKBHYwdAIBAQQg9hemNsCvzOMhuBhozv+z+/HYNS9P9yaR04AbE673BHOgBwYFK4EEAAqhRANCAASiRj1yfNZq/UXh5ImqZtm+vifbdRwK/RKqH4EFgyL86ZoZZ8wLm4cNTYWURQJmCI1nQ/dgI3jYDNpFc95LRhyv";
        String publicKey5 = "f1ACj/OfJ03iwcmuvmYgbVCoRYwES20gfCGzYG7K7dM=-syTfp4KQQZg6xOrmA70qYapMccXZ+gZPGSH+3L8rzIw=";
        String privateKey5 = "MIGNAgEAMBAGByqGSM49AgEGBSuBBAAKBHYwdAIBAQQgJppnMPQtDu4VlRf+EgyQtRPjkZNlj8fkb1eCbLx/h8mgBwYFK4EEAAqhRANCAAR/UAKP858nTeLBya6+ZiBtUKhFjARLbSB8IbNgbsrt07Mk36eCkEGYOsTq5gO9KmGqTHHF2foGTxkh/ty/K8yM";
        String publicKey6 = "EX0Qdh4C9shTQN7l/JBp183+OdJnb4mGTnyH+jfEz6A=-6YWUq8S+ysqimWUxQYN+8M2Tl7ifrwY3BSbUvAiJpLg=";
        String privateKey6 = "MIGNAgEAMBAGByqGSM49AgEGBSuBBAAKBHYwdAIBAQQgUQ0xaT+KGBmxg+NBQxks/aC9LZtg1NeMnMaKNIMQWO+gBwYFK4EEAAqhRANCAAQRfRB2HgL2yFNA3uX8kGnXzf450mdviYZOfIf6N8TPoOmFlKvEvsrKopllMUGDfvDNk5e4n68GNwUm1LwIiaS4";
        String publicKey7 = "soysqaBEmffmBubA6TOhu9Fu+pzF122q9RgtiMwCg8g=-AMQmGOvOgsWLGoCyLIfsleyayjLS2v7oVE1Es1oMx44=";
        String privateKey7 = "MIGNAgEAMBAGByqGSM49AgEGBSuBBAAKBHYwdAIBAQQgURojZX7solaEZKiNFpHkptEMisNw/5PyQxP/Eq3ngIegBwYFK4EEAAqhRANCAASyjKypoESZ9+YG5sDpM6G70W76nMXXbar1GC2IzAKDyADEJhjrzoLFixqAsiyH7JXsmsoy0tr+6FRNRLNaDMeO";
        String publicKey8 = "u7Ql5/tgpzegTpFVMKP/78HzlX0M0E3svIjC1P2rJmg=-gQDRsc92osHrCgTuN7yKJZ3J3r8IjrJgal08XfqEAS4=";
        String privateKey8 = "MIGNAgEAMBAGByqGSM49AgEGBSuBBAAKBHYwdAIBAQQgfbuv5kylW1+JhjiN4NU6+FgLr5b7z9M5Tinro6pwrPCgBwYFK4EEAAqhRANCAAS7tCXn+2CnN6BOkVUwo//vwfOVfQzQTey8iMLU/asmaIEA0bHPdqLB6woE7je8iiWdyd6/CI6yYGpdPF36hAEu";


        Button buttonAdminAgent = findViewById(R.id.buttonAdminAgent);
        buttonAdminAgent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AdminAgentActivity.class);
                intent.putExtra("key1", publicKey1);
                intent.putExtra("key2", publicKey2);
                intent.putExtra("key3", publicKey3);
                intent.putExtra("key4", publicKey4);
                intent.putExtra("key5", publicKey5);
                intent.putExtra("key6", publicKey6);
                intent.putExtra("key7", publicKey7);
                intent.putExtra("key8", publicKey8);
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

        Button buttonVoterAgent3 = findViewById(R.id.buttonVoterAgent3);
        buttonVoterAgent3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, VoterAgentActivity.class);
                intent.putExtra("publicKey", publicKey3);
                intent.putExtra("privateKey", privateKey3);
                startActivity(intent);
            }
        });

        Button buttonVoterAgent4 = findViewById(R.id.buttonVoterAgent4);
        buttonVoterAgent4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, VoterAgentActivity.class);
                intent.putExtra("publicKey", publicKey4);
                intent.putExtra("privateKey", privateKey4);
                startActivity(intent);
            }
        });

        Button buttonVoterAgent5 = findViewById(R.id.buttonVoterAgent5);
        buttonVoterAgent5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, VoterAgentActivity.class);
                intent.putExtra("publicKey", publicKey5);
                intent.putExtra("privateKey", privateKey5);
                startActivity(intent);
            }
        });

        Button buttonVoterAgent6 = findViewById(R.id.buttonVoterAgent6);
        buttonVoterAgent6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, VoterAgentActivity.class);
                intent.putExtra("publicKey", publicKey6);
                intent.putExtra("privateKey", privateKey6);
                startActivity(intent);
            }
        });

        Button buttonVoterAgent7 = findViewById(R.id.buttonVoterAgent7);
        buttonVoterAgent7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, VoterAgentActivity.class);
                intent.putExtra("publicKey", publicKey7);
                intent.putExtra("privateKey", privateKey7);
                startActivity(intent);
            }
        });

        Button buttonVoterAgent8 = findViewById(R.id.buttonVoterAgent8);
        buttonVoterAgent8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, VoterAgentActivity.class);
                intent.putExtra("publicKey", publicKey8);
                intent.putExtra("privateKey", privateKey8);
                startActivity(intent);
            }
        });

        Button buttonTest = findViewById(R.id.buttonTest);
        buttonTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(MainActivity.this, TestActivity.class);
//                startActivity(intent);

                ArrayList<String> candidates = new ArrayList<>(); // The names of the candidates
                ArrayList<Integer> candidateVotes = new ArrayList<>(); // The votes of the candidates
                ArrayList<String> voters = new ArrayList<>(); // The public key of the voters
                ArrayList<Boolean> voterVoted = new ArrayList<>(); // Whether the voters had voted

                candidates.add("Alice");
                candidates.add("Bob");
                candidateVotes.add(3);
                candidateVotes.add(2);
                voters.add("Carol");
                voters.add("Dean");
                voterVoted.add(true);
                voterVoted.add(true);

                Intent intent = new Intent(MainActivity.this, AdminAgentResultActivity.class);
                Map<String, Integer> candidateVotesMap = new HashMap<>();
                for (int i = 0; i < candidates.size(); i++) {
                    candidateVotesMap.put(candidates.get(i), candidateVotes.get(i));
                }

                ArrayList<String> rticketList = new ArrayList<>();
                for (int i = 0; i < voters.size(); i++) {
                    if (voterVoted.get(i)) {
                        rticketList.add(voters.get(i));
                    }
                }
                intent.putExtra("mapSerializable", (Serializable) candidateVotesMap);
                intent.putStringArrayListExtra("RTICKET_LIST", rticketList);
                startActivity(intent);
            }
        });
    }
}