// src/main/java/com/example/openaifactchecker/service/OpenAIService.java

package com.example.openaifactchecker.service;

import com.example.openaifactchecker.config.OpenAIConfig;
import com.example.openaifactchecker.dto.FactCheckResultDTO;
import com.example.openaifactchecker.mapper.FactCheckMapper;
import com.example.openaifactchecker.dto.ArticleDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
// Import other necessary packages
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class OpenAIService {

    private static final Logger logger = LoggerFactory.getLogger(OpenAIService.class);

    private final WebClient webClient;
    private final OpenAIConfig openAIConfig;
    private final NewsApiService newsApiService;
    private final ConcurrentHashMap<String, FactCheckResultDTO> cache = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    @Autowired
    public OpenAIService(OpenAIConfig openAIConfig, NewsApiService newsApiService, ObjectMapper objectMapper) {
        this.openAIConfig = openAIConfig;
        this.newsApiService = newsApiService;
        this.webClient = WebClient.builder()
                .baseUrl("https://api.openai.com/v1")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + openAIConfig.getApiKey())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
        this.objectMapper = objectMapper;
    }

    /**
     * Checks the fact and provides supporting news articles.
     *
     * @param statement The statement to fact-check.
     * @return A Mono containing the FactCheckResultDTO.
     */
    public Mono<FactCheckResultDTO> checkFact(String statement) {
        // Check if the result is cached
        FactCheckResultDTO cachedResult = cache.get(statement);
        if (cachedResult != null) {
            logger.info("Fetching result from cache for statement: {}", statement);
            return Mono.just(cachedResult);
        }

        logger.info("Fact checking statement: {}", statement);

        // Fetch articles related to the statement
        return newsApiService.fetchArticles(statement)
                .flatMap(articles -> {
                    // Limit to top 3 articles
                    List<ArticleDTO> limitedArticles = articles.stream().limit(3).collect(Collectors.toList());

                    // Construct the prompt including supporting articles
                    StringBuilder articlesBuilder = new StringBuilder();
                    if (!limitedArticles.isEmpty()) {
                        articlesBuilder.append("Supporting Articles:\n");
                        int count = 1;
                        for (ArticleDTO article : limitedArticles) {
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

                    String prompt = "You are a knowledgeable fact-checking assistant. Determine whether the following statement is true or false. Provide a concise explanation for your conclusion. Respond only with JSON in the following format:\n\n" +
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
                            "    // More articles if available\n" +
                            "  ]\n" +
                            "}\n\n" +
                            "Statement: " + statement + "\n\n" +
                            articlesBuilder.toString();

                    Map<String, Object> requestBody = new HashMap<>();
                    requestBody.put("model", "gpt-4"); // Ensure this model is accessible
                    List<Map<String, String>> messages = new ArrayList<>();
                    messages.add(Map.of("role", "system", "content", "You are a fact-checking assistant."));
                    messages.add(Map.of("role", "user", "content", prompt));
                    requestBody.put("messages", messages);
                    requestBody.put("max_tokens", 500); // Increased tokens to accommodate JSON
                    requestBody.put("temperature", 0.0); // Reduced randomness to ensure consistent JSON output

                    logger.debug("Request Payload to OpenAI: {}", requestBody);

                    return webClient.post()
                            .uri("/chat/completions")
                            .body(BodyInserters.fromValue(requestBody))
                            .retrieve()
                            .onStatus(status -> !status.is2xxSuccessful(),
                                    clientResponse -> clientResponse.bodyToMono(String.class)
                                            .flatMap(errorBody -> {
                                                logger.error("OpenAI API Error: {}", errorBody);
                                                return Mono.error(new RuntimeException("OpenAI API Error: " + errorBody));
                                            }))
                            .bodyToMono(Map.class)
                            .map(response -> {
                                logger.debug("OpenAI Response: {}", response);
                                if (response.containsKey("choices")) {
                                    List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                                    if (!choices.isEmpty() && choices.get(0).containsKey("message")) {
                                        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                                        if (message.containsKey("content")) {
                                            String result = ((String) message.get("content")).trim();
                                            logger.debug("Raw OpenAI Content: {}", result);
                                            FactCheckResultDTO factCheckResult = FactCheckMapper.toFactCheckResultDTO(result);
                                            cache.put(statement, factCheckResult);
                                            return factCheckResult;
                                        }
                                    }
                                }
                                return new FactCheckResultDTO("Unable to determine the fact check result.", List.of());
                            })
                            .retryWhen(Retry.backoff(3, Duration.ofSeconds(2))
                                    .filter(throwable -> throwable instanceof org.springframework.web.reactive.function.client.WebClientResponseException.TooManyRequests)
                                    .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> retrySignal.failure())
                            )
                            .onErrorResume(e -> {
                                logger.error("Error during fact checking: {}", e.getMessage());
                                return Mono.just(new FactCheckResultDTO("An error occurred while checking the fact. Please try again later.", List.of()));
                            });
                });
    }

}
