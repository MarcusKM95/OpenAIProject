package com.example.openaifactchecker.entity;

import com.example.openaifactchecker.dto.ArticleDTO;
import com.example.openaifactchecker.dto.FactCheckResultDTO;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@Entity
public class FactCheckResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String result;

    private String claim; // New field to store the claim

    @OneToMany(mappedBy = "factCheckResult", cascade = CascadeType.ALL)
    private List<Article> articles = new ArrayList<>();

    // Constructors
    public FactCheckResult() {}

    public FactCheckResult(String claim, String result, List<Article> articles) {
        this.claim = claim; // Set the claim
        this.result = result;
        this.articles = articles;
        for (Article article : articles) {
            article.setFactCheckResult(this); // Set the bidirectional relationship
        }
    }

    // Convert to DTO
    public FactCheckResultDTO toDTO() {
        List<ArticleDTO> articleDTOs = articles.stream()
                .map(Article::toDTO)
                .collect(Collectors.toList());
        return new FactCheckResultDTO(claim, result, articleDTOs); // Pass claim to DTO
    }
}
