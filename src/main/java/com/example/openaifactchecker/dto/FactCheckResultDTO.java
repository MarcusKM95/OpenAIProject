// src/main/java/com/example/openaifactchecker/dto/FactCheckResultDTO.java

package com.example.openaifactchecker.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class FactCheckResultDTO {
    private String result;
    private List<ArticleDTO> articles;

    // Constructors
    public FactCheckResultDTO() {}

    public FactCheckResultDTO(String result, List<ArticleDTO> articles) {
        this.result = result;
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
