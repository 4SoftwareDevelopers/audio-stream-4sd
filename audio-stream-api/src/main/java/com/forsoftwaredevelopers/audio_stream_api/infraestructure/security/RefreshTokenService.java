package com.forsoftwaredevelopers.audio_stream_api.infraestructure.security;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.forsoftwaredevelopers.audio_stream_api.infraestructure.persistense.entity.RefreshTokenJPAEntity;
import com.forsoftwaredevelopers.audio_stream_api.infraestructure.persistense.repository.RefreshTokenJPARepository;

@Service
public class RefreshTokenService {

    private final RefreshTokenJPARepository refreshTokenRepository;
    private final long refreshTokenExpiryMs;

    public RefreshTokenService(
            RefreshTokenJPARepository refreshTokenRepository,
            @Value("${app.security.jwt.refresh-token-expiry-ms:86400000}") long refreshTokenExpiryMs) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenExpiryMs = refreshTokenExpiryMs;
    }

    public RefreshTokenJPAEntity createRefreshToken(String userId, String userType) {
        String token = UUID.randomUUID().toString();
        Instant expiryDate = Instant.now().plusMillis(refreshTokenExpiryMs);

        RefreshTokenJPAEntity refreshToken = new RefreshTokenJPAEntity(
                UUID.randomUUID().toString(),
                token,
                userId,
                userType,
                expiryDate
        );

        return refreshTokenRepository.save(refreshToken);
    }

    public Optional<RefreshTokenJPAEntity> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public boolean isValid(RefreshTokenJPAEntity refreshToken) {
        return !refreshToken.isRevoked() && 
               refreshToken.getExpiryDate().isAfter(Instant.now());
    }

    public void revokeToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(rt -> {
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
        });
    }

    public void revokeAllUserTokens(String userId, String userType) {
        refreshTokenRepository.revokeAllByUserIdAndType(userId, userType);
    }

    public void deleteExpiredTokens() {
        refreshTokenRepository.deleteAllExpired(Instant.now());
    }
}