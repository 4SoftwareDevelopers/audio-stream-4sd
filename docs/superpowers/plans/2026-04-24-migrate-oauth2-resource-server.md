# Migrate to Spring OAuth2 Resource Server Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace custom JwtAuthenticationFilter with Spring Security OAuth2 Resource Server for JWT validation

**Architecture:** Add spring-boot-starter-oauth2-resource-server, configure in SecurityConfig, remove custom filter. Keep JwtService for token generation (login flow), migrate WebSocket validation to use Spring's DSL.

**Tech Stack:** Spring Boot 4.0, Spring Security OAuth2 Resource Server, Java 21

---

## Current Architecture

```
Request → RateLimitFilter → JwtAuthenticationFilter (custom) → Controller
         ↓
    Uses: JwtService.validateToken()
    Creates: UsernamePasswordAuthenticationToken with authorities (SCOPE_xxx, TYPE_xxx)
```

## Target Architecture

```
Request → RateLimitFilter → OAuth2 Resource Server (Spring) → Controller
         ↓
    Uses: JWT decoder with custom claims converter
    Creates: OAuth2AuthenticatedPrincipal with authorities
```

## Files to Modify

### Dependencies
- `pom.xml` - Add OAuth2 Resource Server starter

### Configuration
- `SecurityConfig.java` - Configure OAuth2 Resource Server, remove JwtAuthenticationFilter
- `application.properties` - Add JWT issuer/secret config

### Remove
- `JwtAuthenticationFilter.java` - Delete after migration

### Keep (for token generation)
- `JwtService.java` - Used by AuthController for login token generation
- `TokenPayload.java` - Used by WebSocketConfig

### Update
- `WebSocketConfig.java` - Uses jwtService directly, may need adapter

---

## Task 1: Add OAuth2 Resource Server Dependency

**Files:**
- Modify: `pom.xml`

- [ ] **Step 1: Add dependency to pom.xml**

Add after spring-boot-starter-security (around line 44-50):
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>
```

- [ ] **Step 2: Verify dependency resolution**

Run: `./mvnw dependency:resolve -DincludeArtifactIds=spring-boot-starter-oauth2-resource-server`
Expected: RESOLVED

- [ ] **Step 3: Commit**

```bash
git add pom.xml
git commit -m "feat: add OAuth2 Resource Server dependency"
```

---

## Task 2: Configure OAuth2 Resource Server in SecurityConfig

**Files:**
- Modify: `src/main/resources/application.properties`
- Modify: `src/main/java/.../SecurityConfig.java`

- [ ] **Step 1: Add JWT configuration to application.properties**

Add after existing security config (around line 24-26):
```properties
# OAuth2 Resource Server
spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8080
spring.security.oauth2.resourceserver.jwt.secret=${app.security.jwt.secret}
```

- [ ] **Step 2: Add OAuth2 Resource Server configuration to SecurityConfig**

Replace current filter chain configuration with:

```java
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.JwtAuthenticationConverterFactory;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final RateLimitFilter rateLimitFilter;

    public SecurityConfig(RateLimitFilter rateLimitFilter) {
        this.rateLimitFilter = rateLimitFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/ws/**").permitAll()
                .requestMatchers("/api/admin/**").authenticated()
                .anyRequest().permitAll()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
            )
            .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            String type = jwt.getClaimAsString("type");
            List<String> scopes = jwt.getClaimAsStringList("scopes");
            
            var authorities = new ArrayList<GrantedAuthority>();
            if (scopes != null) {
                scopes.forEach(s -> authorities.add(new SimpleGrantedAuthority("SCOPE_" + s)));
            }
            if (type != null) {
                authorities.add(new SimpleGrantedAuthority("TYPE_" + type));
            }
            return authorities;
        });
        return converter;
    }
```

Add imports:
```java
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import java.util.ArrayList;
```

- [ ] **Step 3: Build to verify**

Run: `./mvnw compile`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add application.properties SecurityConfig.java
git commit -m "feat: configure OAuth2 Resource Server for JWT validation"
```

---

## Task 3: Remove Custom JwtAuthenticationFilter

**Files:**
- Delete: `src/main/java/.../security/JwtAuthenticationFilter.java`

- [ ] **Step 1: Verify no remaining references**

Run: `grep -r "JwtAuthenticationFilter" src/main/java/`
Expected: No matches (only in SecurityConfig which we modified)

- [ ] **Step 2: Delete the filter file**

Run: `rm src/main/java/com/forsoftwaredevelopers/audio_stream_api/infraestructure/security/JwtAuthenticationFilter.java`

- [ ] **Step 3: Build to verify**

Run: `./mvnw compile`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add -A
git commit -m "refactor: remove custom JwtAuthenticationFilter, use OAuth2 Resource Server"
```

---

## Task 4: Verify WebSocket Integration

**Files:**
- Modified: `src/main/java/.../config/WebSocketConfig.java`

- [ ] **Step 1: Review WebSocketConfig usage of jwtService**

The WebSocketConfig currently uses `jwtService.validateToken()` directly. OAuth2 Resource Server handles HTTP requests, but WebSocket may need separate handling.

Check if WebSocket works with existing setup by testing or reviewing:

- [ ] **Step 2: If needed, create WebSocket JWT validator**

If WebSocket fails, create a simple JWT validation component that uses JwtService directly (keep it).

Note: This may need investigation. Skip if working.

- [ ] **Step 3: Commit**

```bash
git commit --amend -m "refactor: remove custom JwtAuthenticationFilter, use OAuth2 Resource Server"
```

Or create new commit if WebSocketConfig was modified.

---

## Testing Checklist

- [ ] Login works: `POST /api/auth/login` returns JWT
- [ ] Authenticated endpoint works: `GET /api/admin/...` with Bearer token
- [ ] Invalid token returns 401
- [ ] WebSocket connection works
- [ ] Run tests: `./mvnw test`

---

## Implications Summary

| Aspect | Before | After |
|--------|--------|-------|
| JWT Validation | Custom filter | Spring OAuth2 Server |
| Dependencies | jsonwebtoken (jjwt) | + oauth2-resource-server |
| Code | ~78 lines filter | ~30 lines config |
| Error Handling | Custom JSON | Standard 401 |
| Authorities | Custom (SCOPE_, TYPE_) | Custom converter |
| Token Generation | JwtService | JwtService (keep) |

OAuth2 Resource Server usesJJWT internally, so you can keep your existing secret/key format.

---

## Plan complete and saved to `docs/superpowers/plans/2026-04-24-migrate-oauth2-resource-server.md`. Two execution options:**

**1. Subagent-Driven (recommended)** - I dispatch a fresh subagent per task, review between tasks, fast iteration

**2. Inline Execution** - Execute tasks in this session using executing-plans, batch execution with checkpoints for review

**Which approach?**