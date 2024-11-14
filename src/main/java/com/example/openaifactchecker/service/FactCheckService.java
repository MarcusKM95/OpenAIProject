package com.example.openaifactchecker.service;

import com.example.openaifactchecker.dto.FactCheckResultDTO;
import com.example.openaifactchecker.dto.ArticleDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class FactCheckService {

    private final ConcurrentHashMap<String, FactCheckResultDTO> cache = new ConcurrentHashMap<>();
    private final OpenAIService openAIService;
    private final NewsApiService newsApiService;
    private final FactCheckRequestBuilder factCheckRequestBuilder;

    @Autowired
    public FactCheckService(OpenAIService openAIService, NewsApiService newsApiService, FactCheckRequestBuilder factCheckRequestBuilder) {
        this.openAIService = openAIService;
        this.newsApiService = newsApiService;
        this.factCheckRequestBuilder = factCheckRequestBuilder;
    }

    public Mono<FactCheckResultDTO> checkFact(String statement) {
        // Check if the result is cached
        FactCheckResultDTO cachedResult = cache.get(statement);
        if (cachedResult != null) {
            return Mono.just(cachedResult);
        }

        return newsApiService.fetchArticles(statement)
                .flatMap(articles -> {
                    List<ArticleDTO> limitedArticles = articles.stream().limit(3).toList();
                    String prompt = factCheckRequestBuilder.buildPrompt(statement, limitedArticles);

                    return openAIService.sendRequestToOpenAI(prompt)
                            .map(factCheckResult -> {
                                cache.put(statement, factCheckResult);
                                return factCheckResult;
                            });
                });
    }
}
