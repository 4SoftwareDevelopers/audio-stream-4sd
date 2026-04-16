package com.forsoftwaredevelopers.audio_stream_api.infraestructure.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.forsoftwaredevelopers.audio_stream_api.domain.model.TokenPayload;
import com.forsoftwaredevelopers.audio_stream_api.domain.result.DomainError;
import com.forsoftwaredevelopers.audio_stream_api.domain.result.ErrorType;
import com.forsoftwaredevelopers.audio_stream_api.domain.result.Result;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private final SecretKey secretKey;
    private final long expirationMs;

    public JwtService(
            @Value("${app.security.jwt.secret:}") String secret,
            @Value("${app.security.jwt.expiration-ms:3600000}") long expirationMs) {
        if (secret == null || secret.isBlank()) {
            secret = "test-secret-key-must-be-at-least-256-bits-long-for-hmac-sha256";
        }
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String generateToken(String sub, String type, List<String> scopes) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(expirationMs);

        return Jwts.builder()
                .subject(sub)
                .claim("type", type)
                .claim("scopes", scopes)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(secretKey)
                .compact();
    }

    public Result<TokenPayload> validateToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String sub = claims.getSubject();
            String type = claims.get("type", String.class);
            @SuppressWarnings("unchecked")
            List<String> scopes = claims.get("scopes", List.class);

            if (sub == null || type == null) {
                return Result.fail(new DomainError("INVALID_TOKEN", "Token missing required claims", ErrorType.UNAUTHORIZED));
            }

            return Result.ok(new TokenPayload(sub, type, scopes != null ? scopes : List.of()));
        } catch (ExpiredJwtException e) {
            return Result.fail(new DomainError("TOKEN_EXPIRED", "Token has expired", ErrorType.UNAUTHORIZED));
        } catch (JwtException e) {
            return Result.fail(new DomainError("INVALID_TOKEN", "Invalid token", ErrorType.UNAUTHORIZED));
        }
    }

    public long getExpirationMs() {
        return expirationMs;
    }
}