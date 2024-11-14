package com.example.openaifactchecker.controller;

import com.example.openaifactchecker.dto.FactCheckResultDTO;
import com.example.openaifactchecker.service.FactCheckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:8080") // Adjust as needed
public class FactCheckerController {

    private final FactCheckService factCheckService;

    @Autowired
    public FactCheckerController(FactCheckService factCheckService) {
        this.factCheckService = factCheckService;
    }

    @PostMapping(value = "/check", consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<FactCheckResultDTO> checkFact(@RequestBody String statement) {
        if (statement == null || statement.trim().isEmpty()) {
            return Mono.just(new FactCheckResultDTO("Error: Statement cannot be empty.", List.of()));
        }
        return factCheckService.checkFact(statement);
    }
}
