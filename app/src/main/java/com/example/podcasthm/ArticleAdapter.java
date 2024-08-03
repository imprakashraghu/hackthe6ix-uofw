package com.example.podcasthm;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class ArticleAdapter extends BaseAdapter {

    private Context context;
    private List<Article> articles;

    public ArticleAdapter(Context context, List<Article> articles) {
        this.context = context;
        this.articles = articles;
    }

    @Override
    public int getCount() {
        return articles.size();
    }

    @Override
    public Object getItem(int position) {
        return articles.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_article, parent, false);
        }

        TextView titleTextView = convertView.findViewById(R.id.article_title);
        TextView descriptionTextView = convertView.findViewById(R.id.article_description);

        Article article = articles.get(position);
        Log.d("Test123", "Article: " + article); // Logging article

        titleTextView.setText(article.getTitle());
        descriptionTextView.setText(article.getDescription());

        titleTextView.setOnClickListener(v -> {
            String link = article.getLink();
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
            context.startActivity(browserIntent);
        });

        return convertView;
    }
}
