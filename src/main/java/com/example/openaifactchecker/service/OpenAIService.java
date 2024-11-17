package com.example.openaifactchecker.service;

import com.example.openaifactchecker.config.OpenAIConfig;
import com.example.openaifactchecker.dto.FactCheckResultDTO;
import com.example.openaifactchecker.mapper.FactCheckMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

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

    public Mono<FactCheckResultDTO> sendRequestToOpenAI(String prompt, String claim) {
        // Prepare the request body
        var requestBody = Map.of(
                "model", "gpt-4",
                "messages", List.of(Map.of("role", "system", "content", "You are a fact-checking assistant."),
                        Map.of("role", "user", "content", prompt)),
                "max_tokens", 500,
                "temperature", 0.0); // det er hvor advanceret et svar chat skal komme med

        return webClient.post()
                .uri("/chat/completions")// identifier for chatgpt
                .body(BodyInserters.fromValue(requestBody))
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    // Process OpenAI response
                    String content = ((Map<String, Object>) ((List<Map<String, Object>>) response.get("choices")).get(0).get("message")).get("content").toString().trim();
                    return FactCheckMapper.toFactCheckResultDTO(content, claim);
                });
    }
}
