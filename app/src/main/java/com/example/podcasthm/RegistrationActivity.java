package com.example.podcasthm;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegistrationActivity extends AppCompatActivity {
    private EditText emailEditText, passwordEditText;
    private Button registerButton;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        registerButton = findViewById(R.id.reg_button);

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        registerButton.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(RegistrationActivity.this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            saveUserData(user);
                        } else {
                            Toast.makeText(RegistrationActivity.this, "User not found.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(RegistrationActivity.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveUserData(FirebaseUser user) {
        if (user != null) {
            String userId = user.getUid();

            // Create a map of user data
            Map<String, Object> userData = new HashMap<>();
            userData.put("Email", user.getEmail());
            userData.put("Password", passwordEditText.getText().toString()); // Storing password is generally not recommended

            // Save the user data in Firestore
            firestore.collection("Users").document(userId)
                    .set(userData)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(RegistrationActivity.this, "User registered and data saved.", Toast.LENGTH_SHORT).show();
                            // Navigate to FirstLevelPreferencesActivity
                            Intent intent = new Intent(RegistrationActivity.this, activity_preference_selection.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(RegistrationActivity.this, "Failed to save user data: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        } else {
            Toast.makeText(RegistrationActivity.this, "Failed to get user information.", Toast.LENGTH_SHORT).show();
        }
    }
}
