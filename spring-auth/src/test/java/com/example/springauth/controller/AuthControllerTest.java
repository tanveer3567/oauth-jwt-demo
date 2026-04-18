package com.example.springauth.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AuthController}.
 *
 * The controller has two responsibilities:
 * <ul>
 *   <li>{@code GET /login}  → redirect to {@code /oauth2/authorization/google}</li>
 *   <li>{@code GET /signup} → redirect to {@code /oauth2/authorization/google?prompt=select_account}</li>
 * </ul>
 *
 * No Spring context is started; we test the controller methods directly,
 * mocking only {@link HttpServletResponse}.
 */
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private AuthController authController;

    // -------------------------------------------------------------------------
    // /login
    // -------------------------------------------------------------------------

    @Test
    void login_redirectsToOAuth2AuthorizationGoogle() throws IOException {
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);

        authController.login(response);

        verify(response).sendRedirect(urlCaptor.capture());
        assertThat(urlCaptor.getValue()).isEqualTo("/oauth2/authorization/google");
    }

    @Test
    void login_sendRedirectCalledExactlyOnce() throws IOException {
        authController.login(response);
        verify(response, times(1)).sendRedirect(anyString());
    }

    @Test
    void login_redirectUrlDoesNotContainPromptParam() throws IOException {
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);

        authController.login(response);

        verify(response).sendRedirect(urlCaptor.capture());
        assertThat(urlCaptor.getValue()).doesNotContain("prompt");
    }

    // -------------------------------------------------------------------------
    // /signup
    // -------------------------------------------------------------------------

    @Test
    void signup_redirectsToOAuth2AuthorizationGoogleWithPromptSelectAccount() throws IOException {
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);

        authController.signup(response);

        verify(response).sendRedirect(urlCaptor.capture());
        assertThat(urlCaptor.getValue())
                .isEqualTo("/oauth2/authorization/google?prompt=select_account");
    }

    @Test
    void signup_sendRedirectCalledExactlyOnce() throws IOException {
        authController.signup(response);
        verify(response, times(1)).sendRedirect(anyString());
    }

    @Test
    void signup_redirectUrlContainsPromptSelectAccount() throws IOException {
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);

        authController.signup(response);

        verify(response).sendRedirect(urlCaptor.capture());
        assertThat(urlCaptor.getValue()).contains("prompt=select_account");
    }

    @Test
    void signup_redirectUrlTargetsGoogleRegistration() throws IOException {
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);

        authController.signup(response);

        verify(response).sendRedirect(urlCaptor.capture());
        assertThat(urlCaptor.getValue()).startsWith("/oauth2/authorization/google");
    }

    // -------------------------------------------------------------------------
    // Behavioral distinction between login and signup
    // -------------------------------------------------------------------------

    @Test
    void login_andSignup_redirectToDifferentUrls() throws IOException {
        ArgumentCaptor<String> loginUrl = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> signupUrl = ArgumentCaptor.forClass(String.class);

        // Use separate response mocks so the captors don't interfere.
        HttpServletResponse loginResponse = mock(HttpServletResponse.class);
        HttpServletResponse signupResponse = mock(HttpServletResponse.class);

        authController.login(loginResponse);
        authController.signup(signupResponse);

        verify(loginResponse).sendRedirect(loginUrl.capture());
        verify(signupResponse).sendRedirect(signupUrl.capture());

        assertThat(loginUrl.getValue()).isNotEqualTo(signupUrl.getValue());
    }
}
