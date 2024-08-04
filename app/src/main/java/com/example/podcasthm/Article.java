package com.example.podcasthm;

public class Article {
    private String pubDate;
    private String image;
    private String dumpedAt;
    private String link;
    private String description;
    private String title;

    public Article() {
        // Default constructor required for calls to DataSnapshot.getValue(Article.class)
    }

    public Article(String pubDate, String image, String dumpedAt, String link, String description, String title) {
        this.pubDate = pubDate;
        this.image = image;
        this.dumpedAt = dumpedAt;
        this.link = link;
        this.description = description;
        this.title = title;
    }

    public String getPubDate() {
        return pubDate;
    }

    public void setPubDate(String pubDate) {
        this.pubDate = pubDate;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDumpedAt() {
        return dumpedAt;
    }

    public void setDumpedAt(String dumpedAt) {
        this.dumpedAt = dumpedAt;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
