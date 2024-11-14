package com.example.openaifactchecker.service;

import com.example.openaifactchecker.dto.ArticleDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FactCheckRequestBuilder {

    public String buildPrompt(String statement, List<ArticleDTO> articles) {
        StringBuilder articlesBuilder = new StringBuilder();
        if (!articles.isEmpty()) {
            articlesBuilder.append("Supporting Articles:\n");
            int count = 1;
            for (ArticleDTO article : articles) {
                articlesBuilder.append(count)
                        .append(". ")
                        .append(article.getTitle()).append("\n")
                        .append("   - Author: ").append(article.getAuthor()).append("\n")
                        .append("   - Source: ").append(article.getSource()).append("\n")
                        .append("   - URL: ").append(article.getUrl()).append("\n\n");
                count++;
            }
        } else {
            articlesBuilder.append("No supporting articles found.");
        }

        // Return the final concatenated string directly, no need for toString()
        return "You are a knowledgeable fact-checking assistant. Determine whether the following statement is true or false. Provide a concise explanation for your conclusion. Respond only with JSON in the following format:\n\n" +
                "{\n" +
                "  \"result\": \"True/False\",\n" +
                "  \"explanation\": \"Your explanation here.\",\n" +
                "  \"articles\": [\n" +
                "    {\n" +
                "      \"title\": \"Article Title\",\n" +
                "      \"author\": \"Author Name\",\n" +
                "      \"source\": \"Source Name\",\n" +
                "      \"url\": \"https://example.com/article\"\n" +
                "    }\n" +
                "  ]\n" +
                "}\n\n" +
                "Statement: " + statement + "\n\n" +
                articlesBuilder;
    }
}
