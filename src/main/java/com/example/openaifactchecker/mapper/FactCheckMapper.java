// src/main/java/com/example/openaifactchecker/mapper/FactCheckMapper.java

package com.example.openaifactchecker.mapper;

import com.example.openaifactchecker.dto.ArticleDTO;
import com.example.openaifactchecker.dto.FactCheckResultDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

public class FactCheckMapper {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Converts the raw response string from OpenAI into a FactCheckResultDTO.
     *
     * @param responseContent The raw response content from OpenAI.
     * @return A FactCheckResultDTO object containing the fact check result and supporting articles.
     */
    public static FactCheckResultDTO toFactCheckResultDTO(String responseContent, String claim) {
        try {
            // Parse the response string into a JsonNode
            JsonNode rootNode = objectMapper.readTree(responseContent);

            // Extract fact check result and explanation
            String result = rootNode.has("result") ? rootNode.get("result").asText() : "Unable to determine the fact check result.";
            String explanation = rootNode.has("explanation") ? rootNode.get("explanation").asText() : "";

            // Extract supporting articles
            List<ArticleDTO> articles = new ArrayList<>();
            if (rootNode.has("articles")) {
                for (JsonNode articleNode : rootNode.get("articles")) {
                    String title = articleNode.has("title") ? articleNode.get("title").asText() : "Unknown";
                    String author = articleNode.has("author") ? articleNode.get("author").asText() : "Unknown";
                    String source = articleNode.has("source") ? articleNode.get("source").asText() : "Unknown";
                    String url = articleNode.has("url") ? articleNode.get("url").asText() : "Unknown";

                    articles.add(new ArticleDTO(title, author, source, url));
                }
            }

            // Combine result and explanation if explanation is present
            String combinedResult = explanation.isEmpty() ? result : result + " " + explanation;

            return new FactCheckResultDTO(claim, combinedResult, articles);

        } catch (JsonProcessingException e) {
            // Handle parsing exceptions
            e.printStackTrace();
            return new FactCheckResultDTO(claim, "Unable to parse the fact check result.", List.of());
        }
    }

}
