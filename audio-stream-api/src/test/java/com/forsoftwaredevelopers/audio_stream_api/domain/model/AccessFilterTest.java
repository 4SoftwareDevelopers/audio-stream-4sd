package com.forsoftwaredevelopers.audio_stream_api.domain.model;

import com.forsoftwaredevelopers.audio_stream_api.domain.result.DomainError;
import com.forsoftwaredevelopers.audio_stream_api.domain.result.ErrorType;
import com.forsoftwaredevelopers.audio_stream_api.domain.result.Result;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class AccessFilterTest {

    @Test
    void create_withValidData_returnsOkResultWithBlacklistStatus() {
        String username = "testuser";
        String email = "test@example.com";
        String ip = "192.168.1.1";
        AccessFilterStatus status = AccessFilterStatus.BLACKLIST;

        Result<AccessFilter> result = AccessFilter.create(username, email, ip, status);

        assertTrue(result.isOk());
        AccessFilter filter = result.getOrThrow();
        assertNotNull(filter.getId());
        assertEquals(username, filter.getUsername());
        assertEquals(email, filter.getEmail());
        assertEquals(ip, filter.getIp());
        assertEquals(status, filter.getStatus());
    }

    @Test
    void create_withNullIp_returnsFailResult() {
        String username = "testuser";
        String email = "test@example.com";
        String ip = null;
        AccessFilterStatus status = AccessFilterStatus.BLACKLIST;

        Result<AccessFilter> result = AccessFilter.create(username, email, ip, status);

        assertTrue(result.isFail());
        DomainError error = result.getErrorOrThrow();
        assertEquals(AccessFilter.IP_REQUIRED, error.code());
        assertEquals(ErrorType.VALIDATION, error.type());
    }

    @Test
    void create_withBlankIp_returnsFailResult() {
        String username = "testuser";
        String email = "test@example.com";
        String ip = "   ";
        AccessFilterStatus status = AccessFilterStatus.BLACKLIST;

        Result<AccessFilter> result = AccessFilter.create(username, email, ip, status);

        assertTrue(result.isFail());
        DomainError error = result.getErrorOrThrow();
        assertEquals(AccessFilter.IP_REQUIRED, error.code());
    }

    @Test
    void create_withNullStatus_returnsFailResult() {
        String username = "testuser";
        String email = "test@example.com";
        String ip = "192.168.1.1";
        AccessFilterStatus status = null;

        Result<AccessFilter> result = AccessFilter.create(username, email, ip, status);

        assertTrue(result.isFail());
        DomainError error = result.getErrorOrThrow();
        assertEquals(AccessFilter.INVALID_STATUS, error.code());
        assertEquals(ErrorType.VALIDATION, error.type());
    }

    @Test
    void updateStatus_withValidStatus_returnsOk() {
        String id = "test-id";
        String username = "testuser";
        String email = "test@example.com";
        String ip = "192.168.1.1";
        AccessFilterStatus originalStatus = AccessFilterStatus.BLACKLIST;
        Instant createdAt = Instant.now();
        Instant updatedAt = Instant.now();

        AccessFilter filter = AccessFilter.restore(id, username, email, ip, originalStatus, createdAt, updatedAt);

        Result<Void> result = filter.updateStatus(AccessFilterStatus.BLOCKED);

        assertTrue(result.isOk());
        assertEquals(AccessFilterStatus.BLOCKED, filter.getStatus());
    }

    @Test
    void updateStatus_withNullStatus_returnsFail() {
        String id = "test-id";
        String username = "testuser";
        String email = "test@example.com";
        String ip = "192.168.1.1";
        AccessFilterStatus originalStatus = AccessFilterStatus.BLACKLIST;
        Instant createdAt = Instant.now();
        Instant updatedAt = Instant.now();

        AccessFilter filter = AccessFilter.restore(id, username, email, ip, originalStatus, createdAt, updatedAt);

        Result<Void> result = filter.updateStatus(null);

        assertTrue(result.isFail());
        DomainError error = result.getErrorOrThrow();
        assertEquals(AccessFilter.INVALID_STATUS, error.code());
    }

    @Test
    void restore_reconstructsEntityWithoutValidation() {
        String id = "test-id";
        String username = "testuser";
        String email = "test@example.com";
        String ip = "192.168.1.1";
        AccessFilterStatus status = AccessFilterStatus.WHITELIST;
        Instant createdAt = Instant.now();
        Instant updatedAt = Instant.now();

        AccessFilter filter = AccessFilter.restore(id, username, email, ip, status, createdAt, updatedAt);

        assertEquals(id, filter.getId());
        assertEquals(username, filter.getUsername());
        assertEquals(email, filter.getEmail());
        assertEquals(ip, filter.getIp());
        assertEquals(status, filter.getStatus());
        assertEquals(createdAt, filter.getCreatedAt());
        assertEquals(updatedAt, filter.getUpdatedAt());
    }

    @Test
    void create_withAllFields_returnsOk() {
        String username = "testuser";
        String email = "test@example.com";
        String ip = "192.168.1.1";
        AccessFilterStatus status = AccessFilterStatus.WHITELIST;

        Result<AccessFilter> result = AccessFilter.create(username, email, ip, status);

        assertTrue(result.isOk());
        AccessFilter filter = result.getOrThrow();
        assertNotNull(filter.getId());
        assertNotNull(filter.getCreatedAt());
        assertNotNull(filter.getUpdatedAt());
    }
}