package com.example.podcasthm;

import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Viewsource extends AppCompatActivity {
    private ListView articlesListView;
    private List<Article> articlesList;
    private ArticleAdapter articleAdapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_viewsource);

        articlesListView = findViewById(R.id.articles_list_view);
        articlesList = new ArrayList<>();
        articleAdapter = new ArticleAdapter(this, articlesList);
        articlesListView.setAdapter(articleAdapter);

        db = FirebaseFirestore.getInstance();

        // Load articles from Firestore
        loadArticles();
    }

    private void loadArticles() {
        // Assuming you have a fixed document path or it comes from an Intent
        String selectedCountry = "canada"; // Replace with actual data
        String selectedCategory = "technology"; // Replace with actual data

        // Clear the previous list
        articlesList.clear();

        // Query Firestore based on filters
        db.collection("news-dumps")
                .document("03-08-2024")
                .collection(selectedCountry)
                .document(selectedCategory)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            List<Map<String, Object>> articles = (List<Map<String, Object>>) document.get("articles");
                            if (articles != null) {
                                for (Map<String, Object> articleMap : articles) {
                                    Article article = new Article(
                                            (String) articleMap.get("pub_date"),
                                            (String) articleMap.get("image"),
                                            (String) articleMap.get("dumped_at"),
                                            (String) articleMap.get("link"),
                                            (String) articleMap.get("description"),
                                            (String) articleMap.get("title")
                                    );
                                    articlesList.add(article);
                                }
                                updateArticlesListView();
                            } else {
                                Toast.makeText(Viewsource.this, "No articles found", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(Viewsource.this, "No such document", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(Viewsource.this, "Error getting documents: " + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateArticlesListView() {
        Log.d("FirestoreData", "Article List: " + articlesList.size()); // Logging article list size

        if (articleAdapter == null) {
            articleAdapter = new ArticleAdapter(this, articlesList);
            articlesListView.setAdapter(articleAdapter);
        } else {
            articleAdapter.notifyDataSetChanged();
        }
    }
}
