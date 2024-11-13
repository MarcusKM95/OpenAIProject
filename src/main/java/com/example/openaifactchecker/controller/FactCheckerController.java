package com.example.openaifactchecker.controller;

import com.example.openaifactchecker.service.OpenAIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:8080")
public class FactCheckerController {

    private final OpenAIService openAIService;

    @Autowired
    public FactCheckerController(OpenAIService openAIService) {
        this.openAIService = openAIService;
    }

    @PostMapping(value = "/check", consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public Mono<String> checkFact(@RequestBody String statement) {
        return openAIService.checkFact(statement);
    }
}



