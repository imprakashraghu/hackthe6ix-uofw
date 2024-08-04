package com.example.podcasthm;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ThirdLevelPreferenceActivity extends AppCompatActivity {
    private LinearLayout optionsLayout;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private String country, category;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_third_level_preference);

        optionsLayout = findViewById(R.id.options_layout);

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Retrieve data passed from the previous activity
        country = getIntent().getStringExtra("REGION");
        category = getIntent().getStringExtra("CATEGORY");

        // Log received data for debugging


        // Get the third-level options based on the selected category
        List<String> options = getOptionsForCategory(category);
        for (String option : options) {
            Button optionButton = new Button(this);
            optionButton.setText(option);
            optionButton.setOnClickListener(v -> savePreferences(option));
            optionsLayout.addView(optionButton);
        }
    }

    private List<String> getOptionsForCategory(String category) {
        // Return a list of options based on the category
        if ("technology".equals(category)) {
            return Arrays.asList("Apple", "Tesla");
        } else if ("Health".equals(category)) {
            return Arrays.asList("Weight Loss", "Health Benefits");
        } else if ("Sport".equals(category)) {
            return Arrays.asList("Football", "Basketball");
        } else {
            return Collections.emptyList();
        }
    }

    private void savePreferences(String intrest) {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();

            // Create a map of user preferences
            Map<String, Object> userData = new HashMap<>();
            userData.put("FirstPreference", country);
            userData.put("SecondPreference", category);
            userData.put("ThirdPreference", intrest);

            // Save the user preferences in Firestore
            firestore.collection("Users").document(userId)
                    .collection("Preferences").add(userData)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(ThirdLevelPreferenceActivity.this, "Preferences saved.", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getApplicationContext(), MainPage.class);
                            intent.putExtra("CATEGORY", category);
                            intent.putExtra("COUNTRY",country);
                            startActivity(intent);
                            finish(); // End the current activity
                        } else {
                            Toast.makeText(ThirdLevelPreferenceActivity.this, "Failed to save preferences.", Toast.LENGTH_SHORT).show();
                            Log.e("ThirdLevelPreferenceActivity", "Error saving preferences", task.getException());
                        }
                    });
        } else {
            Toast.makeText(ThirdLevelPreferenceActivity.this, "No user authenticated.", Toast.LENGTH_SHORT).show();
        }
    }
}
