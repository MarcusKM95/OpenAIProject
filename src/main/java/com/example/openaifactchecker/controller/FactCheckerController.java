// src/main/java/com/example/openaifactchecker/controller/FactCheckerController.java

package com.example.openaifactchecker.controller;

import com.example.openaifactchecker.dto.FactCheckResultDTO;
import com.example.openaifactchecker.service.OpenAIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:8080") // Adjust as needed
public class FactCheckerController {

    private final OpenAIService openAIService;

    @Autowired
    public FactCheckerController(OpenAIService openAIService) {
        this.openAIService = openAIService;
    }

    @PostMapping(value = "/check", consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<FactCheckResultDTO> checkFact(@RequestBody String statement) {
        if (statement == null || statement.trim().isEmpty()) {
            return Mono.just(new FactCheckResultDTO("Error: Statement cannot be empty.", List.of()));
        }
        return openAIService.checkFact(statement);
    }
}
