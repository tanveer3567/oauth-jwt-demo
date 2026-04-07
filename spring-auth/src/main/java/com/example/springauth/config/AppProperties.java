package com.example.springauth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private String jwtSecret;
    private String frontendRedirect;

    public String getJwtSecret() { return jwtSecret; }
    public void setJwtSecret(String jwtSecret) { this.jwtSecret = jwtSecret; }
    public String getFrontendRedirect() { return frontendRedirect; }
    public void setFrontendRedirect(String frontendRedirect) { this.frontendRedirect = frontendRedirect; }
}
