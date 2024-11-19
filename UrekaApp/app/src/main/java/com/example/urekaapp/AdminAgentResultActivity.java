package com.example.urekaapp;

import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;

public class AdminAgentResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_agent_result);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        LinearLayout candidateList = findViewById(R.id.candidateList);
        LinearLayout rticketList = findViewById(R.id.candidateAndRticketList);

        // Set the candidates and their votes
        Map<String, Integer> candidateVotesMap = (Map<String, Integer>) getIntent().getSerializableExtra("mapSerializable");
        if (candidateVotesMap != null) {
            StringBuilder candidateListText = new StringBuilder();
            for (Map.Entry<String, Integer> entry : candidateVotesMap.entrySet()) {
                candidateListText.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n\n");
            }

            TextView candidateListView = new TextView(this);
            candidateListView.setTextSize(14);
            candidateListView.setText(candidateListText.toString());
            candidateListView.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            candidateList.addView(candidateListView);
        }

        // Set the public keys of the voters
        ArrayList<String> publicKeys = getIntent().getStringArrayListExtra("RTICKET_LIST");
        if (publicKeys != null) {
            for (String publicKey : publicKeys) {
                LinearLayout keyLayout = new LinearLayout(this);
                keyLayout.setOrientation(LinearLayout.VERTICAL);
                keyLayout.setPadding(16, 16, 16, 16);
                keyLayout.setBackgroundResource(R.drawable.rticket_layout);
                keyLayout.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                ));

                // Create a TextView to display the public key
                TextView keyTextView = new TextView(this);
                keyTextView.setTextSize(16);
                keyTextView.setText("PublicKey = " + publicKey);
                keyTextView.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                ));
                keyTextView.setPadding(0, 0, 0, 10);

                // Create a TextView for the verification text
                TextView verificationTextView = new TextView(this);
                verificationTextView.setTextSize(14);
                verificationTextView.setText("Voted: Verified by voting machine");
                verificationTextView.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                ));

                // Add TextViews to the key layout
                keyLayout.addView(keyTextView);
                keyLayout.addView(verificationTextView);

                // Add the key layout to the main container
                rticketList.addView(keyLayout);
            }
        }
    }
}