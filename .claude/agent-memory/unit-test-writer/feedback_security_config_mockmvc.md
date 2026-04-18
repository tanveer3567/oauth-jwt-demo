---
name: SecurityConfig MockMvc permit-all assertions
description: How to correctly assert that Spring Security permits /login and /signup without blocking
type: feedback
---

Do NOT use `status().is2xxSuccessful()` or `status().is3xxRedirection()` to assert that Spring Security's `permitAll()` is working for `/login` and `/signup` in this project.

**Why:** The two endpoints behave differently under MockMvc:
- `/login` → `AuthController#login` is a `void` method calling `response.sendRedirect()` as a side-effect. MockMvc records a 200 because no explicit status is committed by the method return.
- `/signup` → `AuthController#signup` also calls `response.sendRedirect()` but MockMvc records a 302 (the redirect is actually committed to the MockHttpServletResponse in this case).

The inconsistency makes range assertions (`is2xx`, `is3xx`) unreliable.

**How to apply:** Use a custom `ResultMatcher` that asserts `status NOT IN (401, 403)`:

```java
mockMvc.perform(get("/login"))
    .andExpect(result -> {
        int status = result.getResponse().getStatus();
        assertThat(status).isNotIn(401, 403);
    });
```

This correctly expresses the security intent (Spring Security must not block the endpoint) regardless of what the controller does with the response.
