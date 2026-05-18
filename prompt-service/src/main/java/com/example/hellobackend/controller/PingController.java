package com.example.hellobackend.controller;

import com.example.hellobackend.config.AppProperties;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.health.Status;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
public class PingController {

    private final AppProperties appProperties;
    private final HealthEndpoint healthEndpoint;

    public PingController(AppProperties appProperties, HealthEndpoint healthEndpoint) {
        this.appProperties = appProperties;
        this.healthEndpoint = healthEndpoint;
    }

    @GetMapping("/ping")
    public ResponseEntity<Map<String, String>> ping() {
        Status status = healthEndpoint.health().getStatus();
        boolean isUp = Status.UP.equals(status);
        return ResponseEntity
            .status(isUp ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE)
            .body(Map.of(
                "service", appProperties.getName(),
                "version", appProperties.getVersion(),
                "status", status.getCode(),
                "timestamp", Instant.now().toString()
            ));
    }
}
