// src/main/java/com/example/factchecker/service/OpenAIService.java

package com.example.openaifactchecker.service;

import com.example.openaifactchecker.config.OpenAIConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OpenAIService {

    private final WebClient webClient;
    private final OpenAIConfig openAIConfig;

    @Autowired
    public OpenAIService(OpenAIConfig openAIConfig) {
        this.openAIConfig = openAIConfig;
        this.webClient = WebClient.builder()
                .baseUrl("https://api.openai.com/v1")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + openAIConfig.getApiKey())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public Mono<String> checkFact(String statement) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4"); // Use "gpt-4" or the appropriate model
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", "You are a fact-checking assistant."));
        messages.add(Map.of("role", "user", "content", "Determine if the following statement is fake news. Respond with 'Fake News' or 'Not Fake News':\n\n" + statement));
        requestBody.put("messages", messages);
        requestBody.put("max_tokens", 10);
        requestBody.put("temperature", 0);

        return webClient.post()
                .uri("/chat/completions")
                .body(BodyInserters.fromValue(requestBody))
                .retrieve()
                .onStatus(status -> !status.is2xxSuccessful(),
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> Mono.error(new RuntimeException("API Error: " + errorBody))))
                .bodyToMono(Map.class)
                .map(response -> {
                    if (response.containsKey("choices")) {
                        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                        if (!choices.isEmpty() && choices.get(0).containsKey("message")) {
                            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                            if (message.containsKey("content")) {
                                return ((String) message.get("content")).trim();
                            }
                        }
                    }
                    return "Unable to determine.";
                })
                .onErrorResume(e -> Mono.just("Error: " + e.getMessage()));
    }
}
