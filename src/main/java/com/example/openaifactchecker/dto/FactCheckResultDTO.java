// src/main/java/com/example/openaifactchecker/dto/FactCheckResultDTO.java

package com.example.openaifactchecker.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class FactCheckResultDTO {
    private String claim; // New field
    private String result;
    private List<ArticleDTO> articles;

    public FactCheckResultDTO(String claim, String result, List<ArticleDTO> articles) {
        this.claim = claim;
        this.result = result;
        this.articles = articles;
    }


    // toString (Optional)

    @Override
    public String toString() {
        return "FactCheckResultDTO{" +
                "claim='" + claim + '\'' +
                ", result='" + result + '\'' +
                ", articles=" + articles +
                '}';
    }
}
