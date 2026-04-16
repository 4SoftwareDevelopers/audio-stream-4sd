package com.forsoftwaredevelopers.audio_stream_api.infraestructure.security;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.forsoftwaredevelopers.audio_stream_api.application.command.TokenRequest;
import com.forsoftwaredevelopers.audio_stream_api.domain.result.DomainError;
import com.forsoftwaredevelopers.audio_stream_api.domain.result.ErrorType;
import com.forsoftwaredevelopers.audio_stream_api.domain.result.Result;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private final AuthenticationService authenticationService;
    private final ObjectMapper objectMapper;

    public ApiKeyAuthenticationFilter(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();
        if (!path.equals("/api/auth/token")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            TokenRequest tokenRequest = objectMapper.readValue(request.getInputStream(), TokenRequest.class);

            if (tokenRequest.apiKey() == null || tokenRequest.apiSecret() == null) {
                response.setStatus(HttpStatus.BAD_REQUEST.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter().write("{\"code\":\"MISSING_API_CREDENTIALS\",\"message\":\"API key and secret are required\"}");
                return;
            }

            Result<?> authResult = authenticationService.authenticateWithApiKey(tokenRequest.apiKey(), tokenRequest.apiSecret());

            if (authResult.isFail()) {
                DomainError error = authResult.getErrorOrThrow();
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter().write("{\"code\":\"" + error.code() + "\",\"message\":\"" + error.message() + "\"}");
                return;
            }

            response.setStatus(HttpStatus.OK.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(response.getWriter(), authResult.getOrThrow());

        } catch (IOException e) {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"code\":\"INVALID_REQUEST\",\"message\":\"Invalid request body\"}");
        }
    }
}