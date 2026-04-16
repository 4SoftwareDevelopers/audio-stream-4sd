package com.forsoftwaredevelopers.audio_stream_api.infraestructure.persistense.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "refresh_token")
public class RefreshTokenJPAEntity {

    @Id
    @Column(length = 36)
    private String id;

    @Column(unique = true, nullable = false, length = 512)
    private String token;

    @Column(length = 36)
    private String userId;

    @Column(length = 20)
    private String userType;

    @Column(nullable = false)
    private Instant expiryDate;

    @Column(nullable = false)
    private boolean revoked;

    @Column(nullable = false)
    private Instant createdAt;

    public RefreshTokenJPAEntity() {}

    public RefreshTokenJPAEntity(String id, String token, String userId, String userType, Instant expiryDate) {
        this.id = id;
        this.token = token;
        this.userId = userId;
        this.userType = userType;
        this.expiryDate = expiryDate;
        this.revoked = false;
        this.createdAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }
    public Instant getExpiryDate() { return expiryDate; }
    public void setExpiryDate(Instant expiryDate) { this.expiryDate = expiryDate; }
    public boolean isRevoked() { return revoked; }
    public void setRevoked(boolean revoked) { this.revoked = revoked; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}