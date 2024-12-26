package com.example.urekaapp;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class PermissionlessActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permissionless);

        LinearLayout candidatesLinearLayout = findViewById(R.id.candidatesLinearLayout);
        LinearLayout votersLinearLayout = findViewById(R.id.votersLinearLayout);

        ArrayList<String> candidatesList = getIntent().getStringArrayListExtra("candidatesList");
        ArrayList<String> votersList = getIntent().getStringArrayListExtra("votersList");

        for (String candidate : candidatesList) {
            TextView textView = new TextView(this);
            textView.setText(candidate);
            textView.setTextSize(16);
            textView.setPadding(0, 8, 0, 8);
            candidatesLinearLayout.addView(textView);
        }

        for (String voter : votersList) {
            TextView textView = new TextView(this);
            textView.setText(voter);
            textView.setTextSize(16);
            textView.setPadding(0, 8, 0, 8);
            votersLinearLayout.addView(textView);
        }
    }
}
