package com.example.hellobackend.controller;

import com.example.hellobackend.model.Prompt;
import com.example.hellobackend.security.UserPrincipal;
import com.example.hellobackend.service.PromptService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HelloController {

    private final PromptService promptService;

    public HelloController(PromptService promptService) {
        this.promptService = promptService;
    }

    @GetMapping("/hello")
    public Map<String, String> hello(@AuthenticationPrincipal UserPrincipal principal) {
        return Map.of(
            "message", "Hello, " + principal.getName() + "!",
            "name", principal.getName(),
            "email", principal.getEmail()
        );
    }

    @GetMapping("/prompts")
    public List<Prompt> listPrompts() {
        return promptService.findAll();
    }
}
