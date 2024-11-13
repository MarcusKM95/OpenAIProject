package com.example.openaifactchecker.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NewsApiConfig {

    @Value("${news.api.key}")
    private String apiKey;

    public String getApiKey() {
        return apiKey;
    }
}