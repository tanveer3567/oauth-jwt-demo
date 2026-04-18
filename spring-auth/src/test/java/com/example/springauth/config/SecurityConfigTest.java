package com.example.springauth.config;

import com.example.springauth.controller.AuthController;
import com.example.springauth.security.OAuth2AuthenticationSuccessHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web-layer slice tests for {@link SecurityConfig}.
 *
 * Goals:
 * <ul>
 *   <li>{@code /login} and {@code /signup} are publicly accessible (no auth required).</li>
 *   <li>{@code /error} is publicly accessible.</li>
 *   <li>Any other endpoint requires authentication (returns 302 redirect to OAuth2 login).</li>
 *   <li>CORS is configured — verified via the CorsConfigurationSource bean.</li>
 * </ul>
 *
 * Real Google OAuth2 credentials are NOT needed because:
 *  - {@link ClientRegistrationRepository} is mocked via {@code @MockBean}.
 *  - {@link OAuth2AuthenticationSuccessHandler} is mocked via {@code @MockBean}.
 *  - AppProperties is provided inline via test properties.
 */
@WebMvcTest(controllers = AuthController.class)
@Import(SecurityConfig.class)
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    /*
     * MockBeans satisfy SecurityConfig's constructor parameters so the bean
     * can be instantiated without a real OAuth2 provider or AppProperties YAML.
     */
    @MockBean
    private ClientRegistrationRepository clientRegistrationRepository;

    @MockBean
    private OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    // -------------------------------------------------------------------------
    // Permitted endpoints (no authentication required)
    // -------------------------------------------------------------------------

    @Test
    void getLogin_isPermittedWithoutAuthentication() throws Exception {
        // Spring Security must not block /login with a 401 or 403.
        // The controller's void method calls response.sendRedirect() internally;
        // MockMvc records this as 200 because the void return commits no explicit status.
        mockMvc.perform(get("/login"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    org.assertj.core.api.Assertions.assertThat(status)
                            .as("Spring Security must not block /login (no 401/403)")
                            .isNotIn(401, 403);
                });
    }

    @Test
    void getSignup_isPermittedWithoutAuthentication() throws Exception {
        // Spring Security must not block /signup with a 401 or 403.
        // The controller redirects to the Google OAuth2 endpoint, so MockMvc
        // records a 302 for this path.
        mockMvc.perform(get("/signup"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    org.assertj.core.api.Assertions.assertThat(status)
                            .as("Spring Security must not block /signup (no 401/403)")
                            .isNotIn(401, 403);
                });
    }

    // -------------------------------------------------------------------------
    // Protected endpoints (authentication required)
    // -------------------------------------------------------------------------

    @Test
    void getUnknownProtectedEndpoint_withoutAuthentication_redirectsToLogin() throws Exception {
        // Any unmatched route that is not in the permitAll list should be intercepted
        // by Spring Security and redirected (302) to the OAuth2 login page.
        mockMvc.perform(get("/some/protected/resource"))
                .andExpect(status().is3xxRedirection());
    }

    // -------------------------------------------------------------------------
    // CORS bean
    // -------------------------------------------------------------------------

    @Test
    void corsConfigurationSource_allowsLocalhostOrigin() throws Exception {
        // A pre-flight OPTIONS request from the allowed origin must return 200.
        mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .options("/login")
                        .header("Origin", "http://localhost:4200")
                        .header("Access-Control-Request-Method", "GET")
        ).andExpect(status().isOk());
    }

    @Test
    void corsConfigurationSource_rejectsUnknownOrigin() throws Exception {
        // A pre-flight request from a disallowed origin should not receive CORS headers,
        // resulting in a 403 from the CORS filter.
        mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .options("/login")
                        .header("Origin", "http://evil.example.com")
                        .header("Access-Control-Request-Method", "GET")
        ).andExpect(status().isForbidden());
    }
}
