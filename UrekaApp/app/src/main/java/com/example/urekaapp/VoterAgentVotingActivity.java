package com.example.urekaapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

public class VoterAgentVotingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_voter_agent_voting);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        RadioGroup radioGroup = findViewById(R.id.radioGroup);
        Button buttonConfirm = findViewById(R.id.buttonConfirm);

        ArrayList<String> candidates = getIntent().getStringArrayListExtra("CANDIDATES");
        assert candidates != null;
        int buttonId = 0;
        for (String candidate : candidates) {
            RadioButton radioButton = new RadioButton(this);
            radioButton.setText(candidate);
            radioButton.setTextSize(20);
            radioButton.setId(buttonId++);
            radioGroup.addView(radioButton);
        }

        buttonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int selectedId = radioGroup.getCheckedRadioButtonId();

                if (selectedId != -1) {
                    Intent intent = new Intent();
                    intent.putExtra("VOTED_CANDIDATE", selectedId);
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    Toast.makeText(VoterAgentVotingActivity.this, "Please select a candidate.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Register a callback to handle back button presses
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Toast.makeText(VoterAgentVotingActivity.this, "Please finish your vote.", Toast.LENGTH_SHORT).show();
            }
        };
        // Add the callback to the dispatcher
        getOnBackPressedDispatcher().addCallback(this, callback);
    }
}