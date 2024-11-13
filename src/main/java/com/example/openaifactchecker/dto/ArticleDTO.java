// src/main/java/com/example/openaifactchecker/dto/ArticleDTO.java

package com.example.openaifactchecker.dto;

public class ArticleDTO {
    private String title;
    private String author;
    private String source;
    private String url;

    // Constructors
    public ArticleDTO() {}

    public ArticleDTO(String title, String author, String source, String url) {
        this.title = title;
        this.author = author;
        this.source = source;
        this.url = url;
    }

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }


    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }


    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    // toString (Optional)
    @Override
    public String toString() {
        return "ArticleDTO{" +
                "title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", source='" + source + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
