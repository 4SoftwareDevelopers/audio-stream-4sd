package com.forsoftwaredevelopers.audio_stream_api.domain.model;

import java.time.Instant;
import java.util.UUID;

import com.forsoftwaredevelopers.audio_stream_api.domain.result.DomainError;
import com.forsoftwaredevelopers.audio_stream_api.domain.result.ErrorType;
import com.forsoftwaredevelopers.audio_stream_api.domain.result.Result;

public class AccessFilter {

    public static final String INVALID_STATUS = "INVALID_STATUS";
    public static final String IP_REQUIRED = "IP_REQUIRED";
    public static final String FILTER_NOT_FOUND = "FILTER_NOT_FOUND";

    private String id;
    private String username;
    private String email;
    private String ip;
    private AccessFilterStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    private AccessFilter(String id, String username, String email, String ip, AccessFilterStatus status, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.ip = ip;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    private static Result<AccessFilter> validate(String username, String email, String ip, AccessFilterStatus status) {
        if (ip == null || ip.trim().isBlank()) {
            return Result.fail(new DomainError(IP_REQUIRED, "IP address is required", ErrorType.VALIDATION));
        }

        if (status == null) {
            return Result.fail(new DomainError(INVALID_STATUS, "Status is required", ErrorType.VALIDATION));
        }

        return Result.ok(null);
    }

    public static Result<AccessFilter> create(String username, String email, String ip, AccessFilterStatus status) {
        var validationResult = validate(username, email, ip, status);
        if (validationResult.isFail()) {
            return validationResult.propagate();
        }

        Instant now = Instant.now();
        return Result.ok(new AccessFilter(
            UUID.randomUUID().toString(),
            username,
            email,
            ip,
            status,
            now,
            now
        ));
    }

    public static AccessFilter restore(String id, String username, String email, String ip, AccessFilterStatus status, Instant createdAt, Instant updatedAt) {
        return new AccessFilter(id, username, email, ip, status, createdAt, updatedAt);
    }

    public Result<Void> updateStatus(AccessFilterStatus newStatus) {
        if (newStatus == null) {
            return Result.fail(new DomainError(INVALID_STATUS, "Status is required", ErrorType.VALIDATION));
        }

        this.status = newStatus;
        this.updatedAt = Instant.now();
        return Result.ok(null);
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getIp() {
        return ip;
    }

    public AccessFilterStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}