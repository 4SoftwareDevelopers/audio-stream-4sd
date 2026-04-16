package com.forsoftwaredevelopers.audio_stream_api.infraestructure.security;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.forsoftwaredevelopers.audio_stream_api.domain.model.TokenPayload;
import com.forsoftwaredevelopers.audio_stream_api.domain.result.Result;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        if (path.startsWith("/api/auth/") || path.equals("/ws")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        Result<TokenPayload> validationResult = jwtService.validateToken(token);

        if (validationResult.isFail()) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"code\":\"INVALID_TOKEN\",\"message\":\"Invalid or expired token\"}");
            return;
        }

        TokenPayload payload = validationResult.getOrThrow();

        List<SimpleGrantedAuthority> authorities = payload.scopes().stream()
                .map(scope -> new SimpleGrantedAuthority("SCOPE_" + scope))
                .toList();

        authorities.add(new SimpleGrantedAuthority("TYPE_" + payload.type()));

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                payload.sub(),
                null,
                authorities
        );

        authentication.setDetails(payload);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }
}