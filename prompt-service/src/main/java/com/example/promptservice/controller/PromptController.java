package com.example.promptservice.controller;

import com.example.promptservice.model.Prompt;
import com.example.promptservice.service.PromptService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class PromptController {

    private final PromptService promptService;

    public PromptController(PromptService promptService) {
        this.promptService = promptService;
    }

    @GetMapping("/prompts")
    public List<Prompt> listPrompts() {
        return promptService.findAll();
    }

    @GetMapping("/prompts/{id}")
    public ResponseEntity<Prompt> getPromptById(@PathVariable String id) {
        return promptService.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
