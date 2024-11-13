// src/main/java/com/example/openaifactchecker/dto/FactCheckResultDTO.java

package com.example.openaifactchecker.dto;

import java.util.List;

public class FactCheckResultDTO {
    private String result;
    private List<ArticleDTO> articles;

    // Constructors
    public FactCheckResultDTO() {}

    public FactCheckResultDTO(String result, List<ArticleDTO> articles) {
        this.result = result;
        this.articles = articles;
    }

    // Getters and Setters
    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }


    public List<ArticleDTO> getArticles() {
        return articles;
    }

    public void setArticles(List<ArticleDTO> articles) {
        this.articles = articles;
    }



    // toString (Optional)
    @Override
    public String toString() {
        return "FactCheckResultDTO{" +
                "result='" + result + '\'' +
                ", articles=" + articles +
                '}';
    }
}
