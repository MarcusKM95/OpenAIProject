// src/main/java/com/example/openaifactchecker/entity/Article.java

package com.example.openaifactchecker.entity;

import com.example.openaifactchecker.dto.ArticleDTO;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String title;
    private String author;
    private String source;
    private String url;

    @ManyToOne
    private FactCheckResult factCheckResult;

    // Constructors
    public Article() {}

    public Article(String title, String author, String source, String url) {
        this.title = title;
        this.author = author;
        this.source = source;
        this.url = url;
    }

    // Convert to DTO
    public ArticleDTO toDTO() {
        return new ArticleDTO(title, author, source, url);
    }
}
