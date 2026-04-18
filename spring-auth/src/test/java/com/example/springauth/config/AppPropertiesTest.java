package com.example.springauth.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for AppProperties — a simple POJO bound from application.yml via
 * {@code @ConfigurationProperties(prefix = "app")}.
 *
 * These tests verify the plain getter/setter contract without a Spring context.
 * The binding of real YAML values is exercised by the integration test
 * {@link AppPropertiesIntegrationTest}.
 */
class AppPropertiesTest {

    @Test
    void setJwtSecret_getJwtSecret_roundTrips() {
        AppProperties props = new AppProperties();
        props.setJwtSecret("test-secret-value");
        assertThat(props.getJwtSecret()).isEqualTo("test-secret-value");
    }

    @Test
    void setFrontendRedirect_getFrontendRedirect_roundTrips() {
        AppProperties props = new AppProperties();
        props.setFrontendRedirect("http://localhost:4200/auth/callback");
        assertThat(props.getFrontendRedirect()).isEqualTo("http://localhost:4200/auth/callback");
    }

    @Test
    void defaultState_jwtSecretIsNull() {
        AppProperties props = new AppProperties();
        assertThat(props.getJwtSecret()).isNull();
    }

    @Test
    void defaultState_frontendRedirectIsNull() {
        AppProperties props = new AppProperties();
        assertThat(props.getFrontendRedirect()).isNull();
    }

    @Test
    void setJwtSecret_overwritesPreviousValue() {
        AppProperties props = new AppProperties();
        props.setJwtSecret("first-secret");
        props.setJwtSecret("second-secret");
        assertThat(props.getJwtSecret()).isEqualTo("second-secret");
    }

    @Test
    void setFrontendRedirect_overwritesPreviousValue() {
        AppProperties props = new AppProperties();
        props.setFrontendRedirect("http://old-host/callback");
        props.setFrontendRedirect("http://new-host/callback");
        assertThat(props.getFrontendRedirect()).isEqualTo("http://new-host/callback");
    }

    @Test
    void setJwtSecret_emptyString_storedAsEmptyString() {
        AppProperties props = new AppProperties();
        props.setJwtSecret("");
        assertThat(props.getJwtSecret()).isEqualTo("");
    }

    @Test
    void setFrontendRedirect_emptyString_storedAsEmptyString() {
        AppProperties props = new AppProperties();
        props.setFrontendRedirect("");
        assertThat(props.getFrontendRedirect()).isEqualTo("");
    }

    @Test
    void twoInstances_areIndependent() {
        AppProperties a = new AppProperties();
        AppProperties b = new AppProperties();

        a.setJwtSecret("secret-a");
        b.setJwtSecret("secret-b");

        assertThat(a.getJwtSecret()).isEqualTo("secret-a");
        assertThat(b.getJwtSecret()).isEqualTo("secret-b");
    }
}
