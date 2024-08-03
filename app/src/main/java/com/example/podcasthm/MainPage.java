package com.example.podcasthm;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainPage extends AppCompatActivity {
    private RecyclerView recyclerView;
    private PodcastAdapter podcastAdapter;
    private List<Podcast> podcastList;
    private FirebaseFirestore db;

    private Spinner countrySpinner;
    private Spinner languageSpinner;
    private Spinner categorySpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        podcastList = new ArrayList<>();
        podcastAdapter = new PodcastAdapter(this, podcastList);
        recyclerView.setAdapter(podcastAdapter);

        countrySpinner = findViewById(R.id.country_spinner);
        languageSpinner = findViewById(R.id.language_spinner);
        categorySpinner = findViewById(R.id.category_spinner);

        // Set default values for Spinners
        setupDefaultSpinners();

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Load podcasts based on default filter values
        loadPodcasts();

    }

    private void setupDefaultSpinners() {
        // Create and set adapter for Country Spinner
        ArrayAdapter<String> countryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, new String[]{"canada", "uS"});
        countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        countrySpinner.setAdapter(countryAdapter);
        countrySpinner.setSelection(0); // Set default value to the first item

        // Create and set adapter for Language Spinner
        ArrayAdapter<String> languageAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, new String[]{"english", "french"});
        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        languageSpinner.setAdapter(languageAdapter);
        languageSpinner.setSelection(0); // Set default value to the first item

        // Create and set adapter for Category Spinner
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, new String[]{"top", "technology", "health"});
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);
        categorySpinner.setSelection(0); // Set default value to the first item

    }

    private void loadPodcasts() {
        String selectedCountry = countrySpinner.getSelectedItem().toString();
        String selectedLanguage = languageSpinner.getSelectedItem().toString();
        String selectedCategory = categorySpinner.getSelectedItem().toString();

        // Clear the previous list
        podcastList.clear();

        // Query Firestore based on filters
        db.collection("podcasts-new")
                .document("03-08-2024")
                .collection(selectedCountry)
                .document(selectedCategory + "-" + selectedLanguage)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // Assuming document contains a map
                            Map<String, Object> data = document.getData();
                            if (data != null) {
                                Podcast podcast = new Podcast();
                                podcast.setAudio((String) data.get("audio"));
                                podcast.setContent((String) data.get("content"));
                                podcast.setLanguage((String) data.get("language"));
                                podcast.setThumbnail((String) data.get("thumbnail"));
                                podcast.setTitle((String) data.get("title"));
                                podcastList.add(podcast);
                                podcastAdapter.notifyDataSetChanged();
                            }
                        } else {
                            Toast.makeText(MainPage.this, "No such document", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(MainPage.this, "Error getting documents: " + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
