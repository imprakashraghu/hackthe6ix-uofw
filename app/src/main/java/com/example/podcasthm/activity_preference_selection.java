package com.example.podcasthm;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

// PreferenceSelectionActivity.java
public class activity_preference_selection extends AppCompatActivity {
    private Button worldwideButton, usButton, canadaButton;
    private String email, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preference_selection);

        worldwideButton = findViewById(R.id.worldwide);
        usButton = findViewById(R.id.us);
        canadaButton = findViewById(R.id.canada);

        email = getIntent().getStringExtra("EMAIL");
        password = getIntent().getStringExtra("PASSWORD");

        worldwideButton.setOnClickListener(v -> navigateToSecondLevel("Worldwide"));
        usButton.setOnClickListener(v -> navigateToSecondLevel("United States"));
        canadaButton.setOnClickListener(v -> navigateToSecondLevel("Canada"));
    }

    private void navigateToSecondLevel(String region) {
        Intent intent = new Intent(activity_preference_selection.this, SecondLevelPreferenceActivity.class);
        intent.putExtra("REGION", region);
        intent.putExtra("EMAIL", email);
        intent.putExtra("PASSWORD", password);
        startActivity(intent);
    }
}