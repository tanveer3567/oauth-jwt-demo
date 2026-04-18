package com.example.springauth.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.springauth.config.AppProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.io.IOException;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OAuth2AuthenticationSuccessHandler.
 *
 * The handler:
 *  1. Extracts email + name from the OAuth2User principal.
 *  2. Mints a JWT (HMAC-SHA256, 1-hour expiry) using AppProperties#jwtSecret.
 *  3. Redirects to AppProperties#frontendRedirect + "?token=" + jwt.
 *
 * All external collaborators (request, response, authentication, appProperties)
 * are mocked so this remains a pure unit test with no Spring context.
 */
@ExtendWith(MockitoExtension.class)
class OAuth2AuthenticationSuccessHandlerTest {

    private static final String TEST_JWT_SECRET = "my-very-secret-key-change-in-prod";
    private static final String TEST_FRONTEND_REDIRECT = "http://localhost:4200/auth/callback";
    private static final String TEST_EMAIL = "user@example.com";
    private static final String TEST_NAME = "Test User";

    @Mock
    private AppProperties appProperties;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Authentication authentication;

    @Mock
    private OAuth2User oAuth2User;

    @InjectMocks
    private OAuth2AuthenticationSuccessHandler handler;

    @BeforeEach
    void setUp() {
        when(appProperties.getJwtSecret()).thenReturn(TEST_JWT_SECRET);
        when(appProperties.getFrontendRedirect()).thenReturn(TEST_FRONTEND_REDIRECT);
        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttribute("email")).thenReturn(TEST_EMAIL);
        when(oAuth2User.getAttribute("name")).thenReturn(TEST_NAME);
    }

    // -------------------------------------------------------------------------
    // Redirect URL shape
    // -------------------------------------------------------------------------

    @Test
    void onAuthenticationSuccess_redirectUrlStartsWithFrontendRedirect() throws IOException {
        ArgumentCaptor<String> redirectCaptor = ArgumentCaptor.forClass(String.class);

        handler.onAuthenticationSuccess(request, response, authentication);

        verify(response).sendRedirect(redirectCaptor.capture());
        assertThat(redirectCaptor.getValue())
                .startsWith(TEST_FRONTEND_REDIRECT + "?token=");
    }

    @Test
    void onAuthenticationSuccess_redirectUrlContainsSingleTokenParam() throws IOException {
        ArgumentCaptor<String> redirectCaptor = ArgumentCaptor.forClass(String.class);

        handler.onAuthenticationSuccess(request, response, authentication);

        verify(response).sendRedirect(redirectCaptor.capture());
        String url = redirectCaptor.getValue();
        // Exactly one "?token=" segment — no extra query params accidentally appended.
        assertThat(url).containsOnlyOnce("?token=");
    }

    @Test
    void onAuthenticationSuccess_sendRedirectCalledExactlyOnce() throws IOException {
        handler.onAuthenticationSuccess(request, response, authentication);

        verify(response, times(1)).sendRedirect(anyString());
    }

    // -------------------------------------------------------------------------
    // JWT claims
    // -------------------------------------------------------------------------

    /**
     * Extracts the JWT from the captured redirect URL and decodes it for assertion.
     */
    private DecodedJWT captureAndDecodeJwt() throws IOException {
        ArgumentCaptor<String> redirectCaptor = ArgumentCaptor.forClass(String.class);
        handler.onAuthenticationSuccess(request, response, authentication);
        verify(response).sendRedirect(redirectCaptor.capture());

        String redirectUrl = redirectCaptor.getValue();
        String token = redirectUrl.substring(redirectUrl.indexOf("?token=") + 7);

        return JWT.require(Algorithm.HMAC256(TEST_JWT_SECRET))
                .build()
                .verify(token);
    }

    @Test
    void onAuthenticationSuccess_jwtSubjectIsEmail() throws IOException {
        DecodedJWT decoded = captureAndDecodeJwt();
        assertThat(decoded.getSubject()).isEqualTo(TEST_EMAIL);
    }

    @Test
    void onAuthenticationSuccess_jwtNameClaimMatchesOAuthUserName() throws IOException {
        DecodedJWT decoded = captureAndDecodeJwt();
        assertThat(decoded.getClaim("name").asString()).isEqualTo(TEST_NAME);
    }

    @Test
    void onAuthenticationSuccess_jwtSignedWithConfiguredSecret() throws IOException {
        // Decoding succeeds only if the signature matches — captureAndDecodeJwt() calls
        // JWT.require(...).verify() which throws if the signature is wrong.
        DecodedJWT decoded = captureAndDecodeJwt();
        assertThat(decoded).isNotNull();
    }

    @Test
    void onAuthenticationSuccess_jwtIssuedAtIsRecent() throws IOException {
        Instant before = Instant.now().minusSeconds(5);

        DecodedJWT decoded = captureAndDecodeJwt();

        Instant issuedAt = decoded.getIssuedAtAsInstant();
        assertThat(issuedAt).isAfter(before);
        assertThat(issuedAt).isBefore(Instant.now().plusSeconds(5));
    }

    @Test
    void onAuthenticationSuccess_jwtExpiresApproximatelyOneHourFromNow() throws IOException {
        Instant before = Instant.now();

        DecodedJWT decoded = captureAndDecodeJwt();

        Instant expiresAt = decoded.getExpiresAtAsInstant();
        // Should be ~3600 seconds after issuedAt; allow ±10 s clock tolerance.
        long secondsUntilExpiry = expiresAt.getEpochSecond() - before.getEpochSecond();
        assertThat(secondsUntilExpiry)
                .isGreaterThanOrEqualTo(3590L)
                .isLessThanOrEqualTo(3610L);
    }

    @Test
    void onAuthenticationSuccess_jwtHasIssuedAtClaim() throws IOException {
        DecodedJWT decoded = captureAndDecodeJwt();
        assertThat(decoded.getIssuedAt()).isNotNull();
    }

    @Test
    void onAuthenticationSuccess_jwtHasExpiresAtClaim() throws IOException {
        DecodedJWT decoded = captureAndDecodeJwt();
        assertThat(decoded.getExpiresAt()).isNotNull();
    }

    // -------------------------------------------------------------------------
    // Different user attributes
    // -------------------------------------------------------------------------

    @Test
    void onAuthenticationSuccess_differentEmail_jwtSubjectReflectsNewEmail() throws IOException {
        when(oAuth2User.getAttribute("email")).thenReturn("other@example.com");

        ArgumentCaptor<String> redirectCaptor = ArgumentCaptor.forClass(String.class);
        handler.onAuthenticationSuccess(request, response, authentication);
        verify(response).sendRedirect(redirectCaptor.capture());

        String token = redirectCaptor.getValue().substring(
                redirectCaptor.getValue().indexOf("?token=") + 7);
        DecodedJWT decoded = JWT.require(Algorithm.HMAC256(TEST_JWT_SECRET))
                .build().verify(token);

        assertThat(decoded.getSubject()).isEqualTo("other@example.com");
    }

    @Test
    void onAuthenticationSuccess_nullName_jwtNameClaimIsNull() throws IOException {
        // OAuth2 providers may omit the name attribute in some configurations.
        when(oAuth2User.getAttribute("name")).thenReturn(null);

        ArgumentCaptor<String> redirectCaptor = ArgumentCaptor.forClass(String.class);
        handler.onAuthenticationSuccess(request, response, authentication);
        verify(response).sendRedirect(redirectCaptor.capture());

        String token = redirectCaptor.getValue().substring(
                redirectCaptor.getValue().indexOf("?token=") + 7);
        DecodedJWT decoded = JWT.require(Algorithm.HMAC256(TEST_JWT_SECRET))
                .build().verify(token);

        // The "name" claim should be absent / null rather than throwing an exception.
        assertThat(decoded.getClaim("name").isNull()).isTrue();
    }

    // -------------------------------------------------------------------------
    // AppProperties wiring
    // -------------------------------------------------------------------------

    @Test
    void onAuthenticationSuccess_usesFrontendRedirectFromAppProperties() throws IOException {
        String customRedirect = "http://myfrontend.example.com/callback";
        when(appProperties.getFrontendRedirect()).thenReturn(customRedirect);

        ArgumentCaptor<String> redirectCaptor = ArgumentCaptor.forClass(String.class);
        handler.onAuthenticationSuccess(request, response, authentication);
        verify(response).sendRedirect(redirectCaptor.capture());

        assertThat(redirectCaptor.getValue()).startsWith(customRedirect + "?token=");
    }

    @Test
    void onAuthenticationSuccess_differentJwtSecret_tokenVerifiableWithSameSecret() throws IOException {
        String alternateSecret = "alternate-secret-for-testing-only";
        when(appProperties.getJwtSecret()).thenReturn(alternateSecret);

        ArgumentCaptor<String> redirectCaptor = ArgumentCaptor.forClass(String.class);
        handler.onAuthenticationSuccess(request, response, authentication);
        verify(response).sendRedirect(redirectCaptor.capture());

        String token = redirectCaptor.getValue().substring(
                redirectCaptor.getValue().indexOf("?token=") + 7);

        // Token must verify with the alternate secret, not the default one.
        DecodedJWT decoded = JWT.require(Algorithm.HMAC256(alternateSecret))
                .build().verify(token);
        assertThat(decoded.getSubject()).isEqualTo(TEST_EMAIL);
    }

    @Test
    void onAuthenticationSuccess_differentJwtSecret_tokenNotVerifiableWithWrongSecret()
            throws IOException {
        String alternateSecret = "alternate-secret-for-testing-only";
        when(appProperties.getJwtSecret()).thenReturn(alternateSecret);

        ArgumentCaptor<String> redirectCaptor = ArgumentCaptor.forClass(String.class);
        handler.onAuthenticationSuccess(request, response, authentication);
        verify(response).sendRedirect(redirectCaptor.capture());

        String token = redirectCaptor.getValue().substring(
                redirectCaptor.getValue().indexOf("?token=") + 7);

        // Attempting to verify with the wrong secret must fail.
        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                JWT.require(Algorithm.HMAC256("completely-wrong-secret"))
                        .build().verify(token)
        ).isInstanceOf(com.auth0.jwt.exceptions.JWTVerificationException.class);
    }
}
