package com.example.hellobackend.security;

public class UserPrincipal {
    private final String email;
    private final String name;

    public UserPrincipal(String email, String name) {
        this.email = email;
        this.name = name;
    }

    public String getEmail() { return email; }
    public String getName() { return name; }
}
