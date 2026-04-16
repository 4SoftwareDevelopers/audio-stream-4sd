package com.forsoftwaredevelopers.audio_stream_api.infraestructure.security;

import java.util.List;

import org.springframework.stereotype.Service;

import com.forsoftwaredevelopers.audio_stream_api.application.command.AuthResponse;
import com.forsoftwaredevelopers.audio_stream_api.domain.result.DomainError;
import com.forsoftwaredevelopers.audio_stream_api.domain.result.ErrorType;
import com.forsoftwaredevelopers.audio_stream_api.domain.result.Result;
import com.forsoftwaredevelopers.audio_stream_api.infraestructure.persistense.entity.AdminUserJPAEntity;
import com.forsoftwaredevelopers.audio_stream_api.infraestructure.persistense.entity.ApiKeyJPAEntity;
import com.forsoftwaredevelopers.audio_stream_api.infraestructure.persistense.entity.RefreshTokenJPAEntity;
import com.forsoftwaredevelopers.audio_stream_api.infraestructure.persistense.repository.AdminUserJPARepository;
import com.forsoftwaredevelopers.audio_stream_api.infraestructure.persistense.repository.RefreshTokenJPARepository;

@Service
public class AuthenticationService {

    private final AdminUserJPARepository adminUserRepository;
    private final ApiKeyValidatorService apiKeyValidator;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoderService passwordEncoderService;
    private final RefreshTokenJPARepository refreshTokenRepository;

    public AuthenticationService(
            AdminUserJPARepository adminUserRepository,
            ApiKeyValidatorService apiKeyValidator,
            JwtService jwtService,
            RefreshTokenService refreshTokenService,
            PasswordEncoderService passwordEncoderService,
            RefreshTokenJPARepository refreshTokenRepository) {
        this.adminUserRepository = adminUserRepository;
        this.apiKeyValidator = apiKeyValidator;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.passwordEncoderService = passwordEncoderService;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public Result<AuthResponse> login(String username, String password) {
        AdminUserJPAEntity user = adminUserRepository.findByUsername(username)
                .orElse(null);

        if (user == null || !user.isActivo()) {
            return Result.fail(new DomainError("INVALID_CREDENTIALS", "Invalid username or password", ErrorType.UNAUTHORIZED));
        }

        if (!passwordEncoderService.matches(password, user.getPassword())) {
            return Result.fail(new DomainError("INVALID_CREDENTIALS", "Invalid username or password", ErrorType.UNAUTHORIZED));
        }

        String jwt = jwtService.generateToken(user.getId(), "admin", List.of("admin_access"));
        RefreshTokenJPAEntity refreshToken = refreshTokenService.createRefreshToken(user.getId(), "admin");

        return Result.ok(new AuthResponse(jwt, refreshToken.getToken(), jwtService.getExpirationMs()));
    }

    public Result<AuthResponse> authenticateWithApiKey(String apiKey, String apiSecret) {
        var validation = apiKeyValidator.validateAndParse(apiKey, apiSecret);

        if (validation.isEmpty()) {
            return Result.fail(new DomainError("INVALID_API_KEY", "Invalid API key or secret", ErrorType.UNAUTHORIZED));
        }

        var validated = validation.get();
        String tipo = validated.tipoCliente();
        List<String> scopes = validated.permisos();

        String jwt = jwtService.generateToken(validated.id(), tipo, scopes);

        if ("OBSERVER".equals(tipo)) {
            return Result.ok(new AuthResponse(jwt, null, jwtService.getExpirationMs()));
        }

        RefreshTokenJPAEntity refreshToken = refreshTokenService.createRefreshToken(validated.id(), tipo);
        return Result.ok(new AuthResponse(jwt, refreshToken.getToken(), jwtService.getExpirationMs()));
    }

    public Result<AuthResponse> refreshToken(String refreshTokenStr) {
        var refreshTokenOpt = refreshTokenRepository.findByToken(refreshTokenStr);

        if (refreshTokenOpt.isEmpty()) {
            return Result.fail(new DomainError("INVALID_REFRESH_TOKEN", "Invalid refresh token", ErrorType.UNAUTHORIZED));
        }

        RefreshTokenJPAEntity refreshToken = refreshTokenOpt.get();

        if (!refreshTokenService.isValid(refreshToken)) {
            return Result.fail(new DomainError("EXPIRED_REFRESH_TOKEN", "Refresh token expired or revoked", ErrorType.UNAUTHORIZED));
        }

        String jwt = jwtService.generateToken(refreshToken.getUserId(), refreshToken.getUserType(), List.of());

        if (!"OBSERVER".equals(refreshToken.getUserType())) {
            refreshTokenService.revokeToken(refreshTokenStr);
            RefreshTokenJPAEntity newRefreshToken = refreshTokenService.createRefreshToken(
                    refreshToken.getUserId(), refreshToken.getUserType());
            return Result.ok(new AuthResponse(jwt, newRefreshToken.getToken(), jwtService.getExpirationMs()));
        }

        return Result.ok(new AuthResponse(jwt, null, jwtService.getExpirationMs()));
    }

    public void logout(String userId, String userType) {
        refreshTokenService.revokeAllUserTokens(userId, userType);
    }
}