// src/main/java/com/example/openaifactchecker/service/FactCheckService.java

package com.example.openaifactchecker.service;

import com.example.openaifactchecker.dto.ArticleDTO;
import com.example.openaifactchecker.dto.FactCheckResultDTO;
import com.example.openaifactchecker.entity.Article;
import com.example.openaifactchecker.entity.FactCheckResult;
import com.example.openaifactchecker.respository.ArticleRepository;
import com.example.openaifactchecker.respository.FactCheckResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class FactCheckService {

    private final ConcurrentHashMap<String, FactCheckResultDTO> cache = new ConcurrentHashMap<>();
    private final OpenAIService openAIService;
    private final NewsApiService newsApiService;
    private final FactCheckRequestBuilder factCheckRequestBuilder;
    private final FactCheckResultRepository factCheckResultRepository;
    private final ArticleRepository articleRepository;


    @Autowired
    public FactCheckService(OpenAIService openAIService, NewsApiService newsApiService,
                            FactCheckRequestBuilder factCheckRequestBuilder,
                            FactCheckResultRepository factCheckResultRepository,
                            ArticleRepository articleRepository) {
        this.openAIService = openAIService;
        this.newsApiService = newsApiService;
        this.factCheckRequestBuilder = factCheckRequestBuilder;
        this.factCheckResultRepository = factCheckResultRepository;
        this.articleRepository = articleRepository;
    }

    public Mono<FactCheckResultDTO> checkFact(String statement) {
        // Check if the result is cached
        FactCheckResultDTO cachedResult = cache.get(statement);
        if (cachedResult != null) {
            return Mono.just(cachedResult);
        }

        // Fetch articles and send the prompt to OpenAI
        return newsApiService.fetchArticles(statement)
                .flatMap(articles -> {
                    List<ArticleDTO> limitedArticles = articles.stream().limit(3).toList();
                    String prompt = factCheckRequestBuilder.buildPrompt(statement, limitedArticles);

                    return openAIService.sendRequestToOpenAI(prompt, statement)
                            .map(factCheckResultDTO -> {
                                // Save the result in the database and cache
                                FactCheckResult savedResult = saveFactCheckResult(factCheckResultDTO);
                                FactCheckResultDTO resultDTO = savedResult.toDTO();
                                cache.put(statement, resultDTO);
                                return resultDTO;
                            });
                });
    }

    private FactCheckResult saveFactCheckResult(FactCheckResultDTO dto) {
        // Convert DTO articles to Article entities
        List<Article> articles = dto.getArticles().stream()
                .map(articleDTO -> new Article(
                        articleDTO.getTitle(),
                        articleDTO.getAuthor(),
                        articleDTO.getSource(),
                        articleDTO.getUrl())
                ).collect(Collectors.toList());

        // Create FactCheckResult entity and set its articles
        FactCheckResult factCheckResult = new FactCheckResult(dto.getClaim(), dto.getResult(), articles);

        // Save the FactCheckResult with articles to the database
        return factCheckResultRepository.save(factCheckResult);
    }
}
