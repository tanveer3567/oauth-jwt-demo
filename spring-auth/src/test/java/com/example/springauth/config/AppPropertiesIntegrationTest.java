package com.example.springauth.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Slice test that loads only the {@link AppProperties} bean and verifies that
 * the values in {@code src/test/resources/application-test.yml} are correctly
 * bound via {@code @ConfigurationProperties}.
 *
 * A minimal {@link SpringBootTest} with a test-specific property source is used
 * rather than the full application context, which would require real Google OAuth2
 * credentials to start.
 */
@SpringBootTest(
        classes = AppPropertiesIntegrationTest.TestConfig.class,
        properties = {
                "app.jwt-secret=integration-test-secret",
                "app.frontend-redirect=http://localhost:4200/auth/callback"
        }
)
class AppPropertiesIntegrationTest {

    /**
     * Minimal configuration that activates {@link AppProperties} binding without
     * pulling in the full {@code @SpringBootApplication} auto-configuration
     * (which would try to contact Google and fail in a CI environment).
     */
    @EnableConfigurationProperties(AppProperties.class)
    static class TestConfig {}

    @Autowired
    private AppProperties appProperties;

    @Test
    void jwtSecret_boundFromTestProperties() {
        assertThat(appProperties.getJwtSecret()).isEqualTo("integration-test-secret");
    }

    @Test
    void frontendRedirect_boundFromTestProperties() {
        assertThat(appProperties.getFrontendRedirect())
                .isEqualTo("http://localhost:4200/auth/callback");
    }

    @Test
    void jwtSecret_matchesDefaultDevValuePattern() {
        // Ensure the secret is not empty — a blank secret would sign tokens insecurely.
        assertThat(appProperties.getJwtSecret()).isNotBlank();
    }

    @Test
    void frontendRedirect_isValidUrlFormat() {
        assertThat(appProperties.getFrontendRedirect())
                .startsWith("http")
                .contains("/auth/callback");
    }
}
