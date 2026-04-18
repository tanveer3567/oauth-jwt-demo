package com.example.springauth.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class AuthController {

    @GetMapping("/signup")
    public void signup(HttpServletResponse response) throws IOException {
        response.sendRedirect("/oauth2/authorization/google?prompt=select_account");
    }

    @GetMapping("/login")
    public void login(HttpServletResponse response) throws IOException {
        response.sendRedirect("/oauth2/authorization/google");
    }

}
