package com.example.springauth.security;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CustomAuthorizationRequestResolver.
 *
 * The resolver wraps DefaultOAuth2AuthorizationRequestResolver and adds a "prompt"
 * additional parameter when the incoming HTTP request carries a "prompt" query param.
 *
 * Strategy: use Mockito.mockConstruction to intercept the DefaultOAuth2AuthorizationRequestResolver
 * that is created inside the constructor so we can control what it returns without
 * spinning up a full Spring context or a real ClientRegistrationRepository.
 */
@ExtendWith(MockitoExtension.class)
class CustomAuthorizationRequestResolverTest {

    @Mock
    private ClientRegistrationRepository clientRegistrationRepository;

    @Mock
    private HttpServletRequest httpServletRequest;

    /** Builds a minimal but valid OAuth2AuthorizationRequest for use as a test fixture. */
    private OAuth2AuthorizationRequest buildBaseAuthorizationRequest() {
        return OAuth2AuthorizationRequest.authorizationCode()
                .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                .clientId("test-client-id")
                .redirectUri("http://localhost:8081/login/oauth2/code/google")
                .scopes(java.util.Set.of("openid", "email"))
                .state("test-state")
                .build();
    }

    // -------------------------------------------------------------------------
    // resolve(HttpServletRequest) — single-arg overload
    // -------------------------------------------------------------------------

    @Test
    void resolve_singleArg_nullFromDefaultResolver_returnsNull() {
        try (MockedConstruction<DefaultOAuth2AuthorizationRequestResolver> mocked =
                mockConstruction(DefaultOAuth2AuthorizationRequestResolver.class,
                        (mock, ctx) -> when(mock.resolve(httpServletRequest)).thenReturn(null))) {

            CustomAuthorizationRequestResolver resolver =
                    new CustomAuthorizationRequestResolver(clientRegistrationRepository);

            OAuth2AuthorizationRequest result = resolver.resolve(httpServletRequest);

            assertThat(result).isNull();
        }
    }

    @Test
    void resolve_singleArg_noPromptParam_returnsOriginalRequest() {
        OAuth2AuthorizationRequest base = buildBaseAuthorizationRequest();

        try (MockedConstruction<DefaultOAuth2AuthorizationRequestResolver> mocked =
                mockConstruction(DefaultOAuth2AuthorizationRequestResolver.class,
                        (mock, ctx) -> when(mock.resolve(httpServletRequest)).thenReturn(base))) {

            when(httpServletRequest.getParameter("prompt")).thenReturn(null);

            CustomAuthorizationRequestResolver resolver =
                    new CustomAuthorizationRequestResolver(clientRegistrationRepository);

            OAuth2AuthorizationRequest result = resolver.resolve(httpServletRequest);

            // When no prompt param is present the original request is returned unchanged.
            assertThat(result).isSameAs(base);
            assertThat(result.getAdditionalParameters()).doesNotContainKey("prompt");
        }
    }

    @Test
    void resolve_singleArg_blankPromptParam_returnsOriginalRequest() {
        OAuth2AuthorizationRequest base = buildBaseAuthorizationRequest();

        try (MockedConstruction<DefaultOAuth2AuthorizationRequestResolver> mocked =
                mockConstruction(DefaultOAuth2AuthorizationRequestResolver.class,
                        (mock, ctx) -> when(mock.resolve(httpServletRequest)).thenReturn(base))) {

            // A blank (whitespace-only) value is treated the same as absent.
            when(httpServletRequest.getParameter("prompt")).thenReturn("   ");

            CustomAuthorizationRequestResolver resolver =
                    new CustomAuthorizationRequestResolver(clientRegistrationRepository);

            OAuth2AuthorizationRequest result = resolver.resolve(httpServletRequest);

            assertThat(result).isSameAs(base);
            assertThat(result.getAdditionalParameters()).doesNotContainKey("prompt");
        }
    }

    @Test
    void resolve_singleArg_withPromptSelectAccount_addsPromptToAdditionalParams() {
        OAuth2AuthorizationRequest base = buildBaseAuthorizationRequest();

        try (MockedConstruction<DefaultOAuth2AuthorizationRequestResolver> mocked =
                mockConstruction(DefaultOAuth2AuthorizationRequestResolver.class,
                        (mock, ctx) -> when(mock.resolve(httpServletRequest)).thenReturn(base))) {

            when(httpServletRequest.getParameter("prompt")).thenReturn("select_account");

            CustomAuthorizationRequestResolver resolver =
                    new CustomAuthorizationRequestResolver(clientRegistrationRepository);

            OAuth2AuthorizationRequest result = resolver.resolve(httpServletRequest);

            assertThat(result).isNotNull();
            assertThat(result.getAdditionalParameters())
                    .containsEntry("prompt", "select_account");
        }
    }

    @Test
    void resolve_singleArg_withArbitraryPromptValue_addsItToAdditionalParams() {
        OAuth2AuthorizationRequest base = buildBaseAuthorizationRequest();

        try (MockedConstruction<DefaultOAuth2AuthorizationRequestResolver> mocked =
                mockConstruction(DefaultOAuth2AuthorizationRequestResolver.class,
                        (mock, ctx) -> when(mock.resolve(httpServletRequest)).thenReturn(base))) {

            when(httpServletRequest.getParameter("prompt")).thenReturn("consent");

            CustomAuthorizationRequestResolver resolver =
                    new CustomAuthorizationRequestResolver(clientRegistrationRepository);

            OAuth2AuthorizationRequest result = resolver.resolve(httpServletRequest);

            assertThat(result.getAdditionalParameters())
                    .containsEntry("prompt", "consent");
        }
    }

    @Test
    void resolve_singleArg_promptAdded_doesNotMutateOtherFields() {
        OAuth2AuthorizationRequest base = buildBaseAuthorizationRequest();

        try (MockedConstruction<DefaultOAuth2AuthorizationRequestResolver> mocked =
                mockConstruction(DefaultOAuth2AuthorizationRequestResolver.class,
                        (mock, ctx) -> when(mock.resolve(httpServletRequest)).thenReturn(base))) {

            when(httpServletRequest.getParameter("prompt")).thenReturn("select_account");

            CustomAuthorizationRequestResolver resolver =
                    new CustomAuthorizationRequestResolver(clientRegistrationRepository);

            OAuth2AuthorizationRequest result = resolver.resolve(httpServletRequest);

            // Core fields must be preserved when the prompt is injected.
            assertThat(result.getClientId()).isEqualTo(base.getClientId());
            assertThat(result.getRedirectUri()).isEqualTo(base.getRedirectUri());
            assertThat(result.getState()).isEqualTo(base.getState());
            assertThat(result.getGrantType()).isEqualTo(base.getGrantType());
        }
    }

    // -------------------------------------------------------------------------
    // resolve(HttpServletRequest, String) — two-arg overload
    // -------------------------------------------------------------------------

    @Test
    void resolve_twoArg_nullFromDefaultResolver_returnsNull() {
        try (MockedConstruction<DefaultOAuth2AuthorizationRequestResolver> mocked =
                mockConstruction(DefaultOAuth2AuthorizationRequestResolver.class,
                        (mock, ctx) ->
                                when(mock.resolve(httpServletRequest, "google")).thenReturn(null))) {

            CustomAuthorizationRequestResolver resolver =
                    new CustomAuthorizationRequestResolver(clientRegistrationRepository);

            OAuth2AuthorizationRequest result = resolver.resolve(httpServletRequest, "google");

            assertThat(result).isNull();
        }
    }

    @Test
    void resolve_twoArg_noPromptParam_returnsOriginalRequest() {
        OAuth2AuthorizationRequest base = buildBaseAuthorizationRequest();

        try (MockedConstruction<DefaultOAuth2AuthorizationRequestResolver> mocked =
                mockConstruction(DefaultOAuth2AuthorizationRequestResolver.class,
                        (mock, ctx) ->
                                when(mock.resolve(httpServletRequest, "google")).thenReturn(base))) {

            when(httpServletRequest.getParameter("prompt")).thenReturn(null);

            CustomAuthorizationRequestResolver resolver =
                    new CustomAuthorizationRequestResolver(clientRegistrationRepository);

            OAuth2AuthorizationRequest result = resolver.resolve(httpServletRequest, "google");

            assertThat(result).isSameAs(base);
        }
    }

    @Test
    void resolve_twoArg_withPromptSelectAccount_addsPromptToAdditionalParams() {
        OAuth2AuthorizationRequest base = buildBaseAuthorizationRequest();

        try (MockedConstruction<DefaultOAuth2AuthorizationRequestResolver> mocked =
                mockConstruction(DefaultOAuth2AuthorizationRequestResolver.class,
                        (mock, ctx) ->
                                when(mock.resolve(httpServletRequest, "google")).thenReturn(base))) {

            when(httpServletRequest.getParameter("prompt")).thenReturn("select_account");

            CustomAuthorizationRequestResolver resolver =
                    new CustomAuthorizationRequestResolver(clientRegistrationRepository);

            OAuth2AuthorizationRequest result = resolver.resolve(httpServletRequest, "google");

            assertThat(result.getAdditionalParameters())
                    .containsEntry("prompt", "select_account");
        }
    }

    @Test
    void resolve_twoArg_existingAdditionalParamsPreserved_whenPromptAdded() {
        // If the base request already has additional parameters they must not be wiped out.
        Map<String, Object> existingParams = new HashMap<>();
        existingParams.put("nonce", "abc123");

        OAuth2AuthorizationRequest base = OAuth2AuthorizationRequest.authorizationCode()
                .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                .clientId("test-client-id")
                .redirectUri("http://localhost:8081/login/oauth2/code/google")
                .scopes(java.util.Set.of("openid"))
                .state("test-state")
                .additionalParameters(existingParams)
                .build();

        try (MockedConstruction<DefaultOAuth2AuthorizationRequestResolver> mocked =
                mockConstruction(DefaultOAuth2AuthorizationRequestResolver.class,
                        (mock, ctx) ->
                                when(mock.resolve(httpServletRequest, "google")).thenReturn(base))) {

            when(httpServletRequest.getParameter("prompt")).thenReturn("select_account");

            CustomAuthorizationRequestResolver resolver =
                    new CustomAuthorizationRequestResolver(clientRegistrationRepository);

            OAuth2AuthorizationRequest result = resolver.resolve(httpServletRequest, "google");

            assertThat(result.getAdditionalParameters())
                    .containsEntry("prompt", "select_account")
                    .containsEntry("nonce", "abc123");
        }
    }
}
