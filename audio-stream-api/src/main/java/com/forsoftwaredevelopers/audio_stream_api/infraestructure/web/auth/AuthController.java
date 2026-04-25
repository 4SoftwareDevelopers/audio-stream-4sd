package com.forsoftwaredevelopers.audio_stream_api.infraestructure.web.auth;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.forsoftwaredevelopers.audio_stream_api.application.command.AuthResponse;
import com.forsoftwaredevelopers.audio_stream_api.application.command.LoginRequest;
import com.forsoftwaredevelopers.audio_stream_api.application.command.RefreshTokenRequest;
import com.forsoftwaredevelopers.audio_stream_api.application.command.TokenRequest;
import com.forsoftwaredevelopers.audio_stream_api.domain.result.DomainError;
import com.forsoftwaredevelopers.audio_stream_api.domain.result.Result;
import com.forsoftwaredevelopers.audio_stream_api.infraestructure.security.AuthenticationService;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("api/auth")
public class AuthController {

    private final AuthenticationService authenticationService;
    private final ObjectMapper objectMapper;

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
        this.objectMapper = new ObjectMapper();
    }

    @PostMapping("/login")
    public void login(@RequestBody LoginRequest request, HttpServletResponse response) throws IOException {
        Result<AuthResponse> result = authenticationService.login(
                request.username(), request.password());

        if (result.isFail()) {
            writeError(response, result.getErrorOrThrow(), HttpStatus.UNAUTHORIZED);
            return;
        }

        writeResponse(response, result.getOrThrow());
    }

    @PostMapping("/token")
    public void getToken(@RequestBody TokenRequest request, HttpServletResponse response) throws IOException {
        Result<AuthResponse> result = authenticationService.authenticateWithApiKey(
                request.apiKey(), request.apiSecret());

        if (result.isFail()) {
            writeError(response, result.getErrorOrThrow(), HttpStatus.UNAUTHORIZED);
            return;
        }

        writeResponse(response, result.getOrThrow());
    }

    @PostMapping("/refresh")
    public void refreshToken(@RequestBody RefreshTokenRequest request, HttpServletResponse response) throws IOException {
        Result<AuthResponse> result = authenticationService.refreshToken(request.refreshToken());

        if (result.isFail()) {
            writeError(response, result.getErrorOrThrow(), HttpStatus.UNAUTHORIZED);
            return;
        }

        writeResponse(response, result.getOrThrow());
    }

    @PostMapping("/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String userId = authentication.getName();
            String userType = authentication.getAuthorities().stream()
                    .filter(a -> a.getAuthority().startsWith("TYPE_"))
                    .map(a -> a.getAuthority().substring(5))
                    .findFirst()
                    .orElse("admin");
            authenticationService.logout(userId, userType);
        }
        response.setStatus(HttpStatus.NO_CONTENT.value());
    }

    private void writeResponse(HttpServletResponse response, Object data) throws IOException {
        response.setStatus(HttpStatus.OK.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), data);
    }

    private void writeError(HttpServletResponse response, DomainError error, HttpStatus status) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"code\":\"" + error.code() + "\",\"message\":\"" + error.message() + "\",\"type\":\"" + error.type() + "\"}");
    }
}