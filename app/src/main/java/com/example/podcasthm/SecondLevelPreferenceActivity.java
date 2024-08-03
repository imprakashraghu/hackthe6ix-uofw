package com.example.podcasthm;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;


public class SecondLevelPreferenceActivity extends AppCompatActivity {
    private Button techButton, healthButton, sportButton;
    private String email, password, region;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second_level_preference);

        techButton = findViewById(R.id.tech);
        healthButton = findViewById(R.id.health);
        sportButton = findViewById(R.id.sport);

        email = getIntent().getStringExtra("EMAIL");
        password = getIntent().getStringExtra("PASSWORD");
        region = getIntent().getStringExtra("REGION");

        techButton.setOnClickListener(v -> navigateToThirdLevel("technology"));
        healthButton.setOnClickListener(v -> navigateToThirdLevel("Health"));
        sportButton.setOnClickListener(v -> navigateToThirdLevel("Sport"));
    }

    private void navigateToThirdLevel(String category) {
        Intent intent = new Intent(SecondLevelPreferenceActivity.this, ThirdLevelPreferenceActivity.class);
        intent.putExtra("CATEGORY", category);
        intent.putExtra("REGION", region);
        intent.putExtra("EMAIL", email);
        intent.putExtra("PASSWORD", password);
        startActivity(intent);
    }
}