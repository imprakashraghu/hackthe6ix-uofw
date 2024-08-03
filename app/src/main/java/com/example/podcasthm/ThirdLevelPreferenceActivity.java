package com.example.podcasthm;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

// ThirdLevelPreferenceActivity.java
public class ThirdLevelPreferenceActivity extends AppCompatActivity {
    private RadioGroup thirdLevelPreferences;
    private Button finishButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_third_level_preference);

        thirdLevelPreferences = findViewById(R.id.third_level_preferences);
        finishButton = findViewById(R.id.finish_button);

        String secondLevelPreference = getIntent().getStringExtra("secondLevelPreference");
        populateThirdLevelOptions(secondLevelPreference);

        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedId = thirdLevelPreferences.getCheckedRadioButtonId();
                String selectedPreference = ((RadioButton) findViewById(selectedId)).getText().toString();

                // Save the selected preferences to Supabase and finish registration
                savePreferencesToSupabase(selectedPreference);
            }
        });
    }

    private void populateThirdLevelOptions(String secondLevelPreference) {
        // Populate third level preferences based on second level selection
        if (secondLevelPreference.equals("Technology")) {
            addRadioButton("Apple");
            addRadioButton("Tesla");
        } else if (secondLevelPreference.equals("Health")) {
            addRadioButton("Weight Loss");
            addRadioButton("Health Benefits");
        } else if (secondLevelPreference.equals("Sport")) {
            addRadioButton("Football");
            addRadioButton("Basketball");
        }
    }

    private void addRadioButton(String text) {
        RadioButton radioButton = new RadioButton(this);
        radioButton.setText(text);
        thirdLevelPreferences.addView(radioButton);
    }

    private void savePreferencesToSupabase(String thirdLevelPreference) {

    }
}
