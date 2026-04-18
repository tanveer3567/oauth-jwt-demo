---
name: spring-auth test suite
description: State of the spring-auth unit test suite written from scratch in April 2026
type: project
---

51 tests written and passing across 5 test classes for the spring-auth service.

**Why:** No automated tests existed before this session; tests were added from scratch.

**How to apply:** When adding new features to spring-auth, mirror the package structure under `src/test/java/com/example/springauth/` and follow the naming convention `methodName_scenario_expectedResult()`.

## Key decisions

- Spring Boot upgraded from 3.2.0 → 3.4.4 to support Java 25 (Byte Buddy 1.14.x in SB 3.2 only supports up to Java 22; Byte Buddy 1.15.x in SB 3.4 supports Java 25).
- `maven-surefire-plugin` configured with `-Dnet.bytebuddy.experimental=true` argLine as belt-and-suspenders.
- `mockito-inline` NOT needed as a separate dependency — `mockito-core` (via `spring-boot-starter-test`) already includes `MockedConstruction` support in SB 3.4.
- `CustomAuthorizationRequestResolverTest` uses `Mockito.mockConstruction()` to intercept the `DefaultOAuth2AuthorizationRequestResolver` created inside the constructor, avoiding any need for a real `ClientRegistrationRepository`.
- `SecurityConfigTest` uses `@WebMvcTest` + `@Import(SecurityConfig.class)` with `@MockBean` for `ClientRegistrationRepository` and `OAuth2AuthenticationSuccessHandler`.

## Test file locations

- `src/test/java/com/example/springauth/security/CustomAuthorizationRequestResolverTest.java` — 10 tests
- `src/test/java/com/example/springauth/security/OAuth2AuthenticationSuccessHandlerTest.java` — 15 tests
- `src/test/java/com/example/springauth/config/AppPropertiesTest.java` — 9 tests
- `src/test/java/com/example/springauth/config/AppPropertiesIntegrationTest.java` — 4 tests
- `src/test/java/com/example/springauth/controller/AuthControllerTest.java` — 8 tests
- `src/test/java/com/example/springauth/config/SecurityConfigTest.java` — 5 tests
