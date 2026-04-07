package com.example.hellobackend.controller;

import com.example.hellobackend.security.UserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class HelloController {

    @GetMapping("/hello")
    public Map<String, String> hello(@AuthenticationPrincipal UserPrincipal principal) {
        return Map.of("message", "Hello, " + principal.getName() + "!");
    }
}
