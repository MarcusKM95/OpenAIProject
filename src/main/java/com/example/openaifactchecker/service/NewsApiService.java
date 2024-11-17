package com.example.openaifactchecker.service;

import com.example.openaifactchecker.config.NewsApiConfig;
import com.example.openaifactchecker.dto.ArticleDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class NewsApiService {

    private static final Logger logger = LoggerFactory.getLogger(NewsApiService.class);

    private final WebClient webClient;
    private final NewsApiConfig newsApiConfig;

    @Autowired
    public NewsApiService(NewsApiConfig newsApiConfig) {
        this.newsApiConfig = newsApiConfig;
        this.webClient = WebClient.builder()
                .baseUrl("https://newsapi.org/v2")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    /**
     * Fetches relevant news articles based on the provided query.
     *
     * @param query The search query (user's statement).
     * @return A Mono containing a list of ArticleDTOs.
     */
    public Mono<List<ArticleDTO>> fetchArticles(String query) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder //kig nærmere om uriBuilder
                        .path("/everything")
                        .queryParam("q", query)
                        .queryParam("sortBy", "relevancy")
                        .queryParam("language", "en")
                        .build())
                .header("Authorization", newsApiConfig.getApiKey())
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> (List<Map<String, Object>>) response.get("articles"))
                .map(articles -> articles.stream()
                        .filter(article -> article.get("author") != null && article.get("source") != null)
                        .map(article -> new ArticleDTO(
                                (String) article.get("title"),
                                (String) article.get("author"),
                                ((Map<String, Object>) article.get("source")).get("name") != null ?
                                        (String) ((Map<String, Object>) article.get("source")).get("name") : "Unknown",
                                (String) article.get("url")
                        ))
                        .collect(Collectors.toList()))
                .onErrorResume(e -> {
                    logger.error("Error fetching articles: {}", e.getMessage());
                    return Mono.just(List.of());
                });
    }
}
