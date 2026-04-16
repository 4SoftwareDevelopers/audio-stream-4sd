package com.forsoftwaredevelopers.audio_stream_api.application.service;

import com.forsoftwaredevelopers.audio_stream_api.application.command.ListAccessFilterCommand;
import com.forsoftwaredevelopers.audio_stream_api.application.command.PagedResponse;
import com.forsoftwaredevelopers.audio_stream_api.application.usecase.AccessFilterUseCases;
import com.forsoftwaredevelopers.audio_stream_api.domain.model.AccessFilter;
import com.forsoftwaredevelopers.audio_stream_api.domain.model.AccessFilterStatus;
import com.forsoftwaredevelopers.audio_stream_api.domain.port.AccessFilterRepository;
import com.forsoftwaredevelopers.audio_stream_api.domain.result.Result;
import com.forsoftwaredevelopers.audio_stream_api.infraestructure.persistense.repository.AccessFilterJPARepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class AccessFilterServiceIntegrationTest {

    @Autowired
    private AccessFilterService accessFilterService;

    @Autowired
    private AccessFilterRepository accessFilterRepository;

    @Autowired
    private AccessFilterJPARepository accessFilterJPARepository;

    @BeforeEach
    void setUp() {
        accessFilterJPARepository.deleteAll();
    }

    @Test
    void create_withValidData_createsFilter() {
        AccessFilterUseCases.CreateAccessFilterCommand command = new AccessFilterUseCases.CreateAccessFilterCommand(
                "testuser", "test@example.com", "192.168.1.1", "BLACKLIST");

        Result<AccessFilter> result = accessFilterService.create(command);

        assertTrue(result.isOk());
        AccessFilter filter = result.getOrThrow();
        assertNotNull(filter.getId());
        assertEquals("testuser", filter.getUsername());
        assertEquals("test@example.com", filter.getEmail());
        assertEquals("192.168.1.1", filter.getIp());
        assertEquals(AccessFilterStatus.BLACKLIST, filter.getStatus());
    }

    @Test
    void create_withWhitelistedStatus_createsFilter() {
        AccessFilterUseCases.CreateAccessFilterCommand command = new AccessFilterUseCases.CreateAccessFilterCommand(
                "testuser", "test@example.com", "192.168.1.1", "WHITELIST");

        Result<AccessFilter> result = accessFilterService.create(command);

        assertTrue(result.isOk());
        assertEquals(AccessFilterStatus.WHITELIST, result.getOrThrow().getStatus());
    }

    @Test
    void create_withInvalidStatus_returnsFail() {
        AccessFilterUseCases.CreateAccessFilterCommand command = new AccessFilterUseCases.CreateAccessFilterCommand(
                "testuser", "test@example.com", "192.168.1.1", "INVALID_STATUS");

        Result<AccessFilter> result = accessFilterService.create(command);

        assertTrue(result.isFail());
    }

    @Test
    void update_withValidStatus_updatesFilter() {
        AccessFilterUseCases.CreateAccessFilterCommand createCommand = new AccessFilterUseCases.CreateAccessFilterCommand(
                "testuser", "test@example.com", "192.168.1.1", "BLACKLIST");
        Result<AccessFilter> createResult = accessFilterService.create(createCommand);
        String filterId = createResult.getOrThrow().getId();

        AccessFilterUseCases.UpdateAccessFilterCommand updateCommand = new AccessFilterUseCases.UpdateAccessFilterCommand(
                filterId, "BLOCKED");

        Result<AccessFilter> result = accessFilterService.update(updateCommand);

        assertTrue(result.isOk());
        assertEquals(AccessFilterStatus.BLOCKED, result.getOrThrow().getStatus());
    }

    @Test
    void update_withNonExistentId_returnsFail() {
        AccessFilterUseCases.UpdateAccessFilterCommand command = new AccessFilterUseCases.UpdateAccessFilterCommand(
                "non-existent-id", "BLOCKED");

        Result<AccessFilter> result = accessFilterService.update(command);

        assertTrue(result.isFail());
        assertTrue(result.getErrorOrThrow().code().contains("FILTER_NOT_FOUND"));
    }

    @Test
    void delete_withExistingId_deletesFilter() {
        AccessFilterUseCases.CreateAccessFilterCommand createCommand = new AccessFilterUseCases.CreateAccessFilterCommand(
                "testuser", "test@example.com", "192.168.1.1", "BLACKLIST");
        Result<AccessFilter> createResult = accessFilterService.create(createCommand);
        String filterId = createResult.getOrThrow().getId();

        AccessFilterUseCases.DeleteAccessFilterCommand deleteCommand = new AccessFilterUseCases.DeleteAccessFilterCommand(filterId);

        Result<Void> result = accessFilterService.delete(deleteCommand);

        assertTrue(result.isOk());
        AccessFilter deletedFilter = accessFilterRepository.findById(filterId);
        assertNull(deletedFilter);
    }

    @Test
    void delete_withNonExistentId_returnsFail() {
        AccessFilterUseCases.DeleteAccessFilterCommand command = new AccessFilterUseCases.DeleteAccessFilterCommand("non-existent-id");

        Result<Void> result = accessFilterService.delete(command);

        assertTrue(result.isFail());
    }

    @Test
    void list_withNoFilters_returnsAllFilters() {
        accessFilterService.create(new AccessFilterUseCases.CreateAccessFilterCommand("user1", "user1@test.com", "192.168.1.1", "BLACKLIST"));
        accessFilterService.create(new AccessFilterUseCases.CreateAccessFilterCommand("user2", "user2@test.com", "192.168.1.2", "WHITELIST"));

        PagedResponse<AccessFilter> result = accessFilterService.list(
                ListAccessFilterCommand.of(null, null, null, null, null, null, 0, 20));

        assertEquals(2, result.content().size());
        assertEquals(2, result.totalElements());
    }

    @Test
    void list_withStatusFilter_returnsFilteredResults() {
        accessFilterService.create(new AccessFilterUseCases.CreateAccessFilterCommand("user1", "user1@test.com", "192.168.1.1", "BLACKLIST"));
        accessFilterService.create(new AccessFilterUseCases.CreateAccessFilterCommand("user2", "user2@test.com", "192.168.1.2", "WHITELIST"));

        PagedResponse<AccessFilter> result = accessFilterService.list(
                ListAccessFilterCommand.of(null, null, null, "BLACKLIST", null, null, 0, 20));

        assertEquals(1, result.content().size());
        assertEquals(AccessFilterStatus.BLACKLIST, result.content().get(0).getStatus());
    }

    @Test
    void list_withIpFilter_returnsFilteredResults() {
        accessFilterService.create(new AccessFilterUseCases.CreateAccessFilterCommand("user1", "user1@test.com", "192.168.1.1", "BLACKLIST"));
        accessFilterService.create(new AccessFilterUseCases.CreateAccessFilterCommand("user2", "user2@test.com", "192.168.1.2", "WHITELIST"));

        PagedResponse<AccessFilter> result = accessFilterService.list(
                ListAccessFilterCommand.of(null, null, "192.168.1.1", null, null, null, 0, 20));

        assertEquals(1, result.content().size());
        assertEquals("192.168.1.1", result.content().get(0).getIp());
    }

    @Test
    void getById_withExistingId_returnsFilter() {
        AccessFilterUseCases.CreateAccessFilterCommand createCommand = new AccessFilterUseCases.CreateAccessFilterCommand(
                "testuser", "test@example.com", "192.168.1.1", "BLACKLIST");
        Result<AccessFilter> createResult = accessFilterService.create(createCommand);
        String filterId = createResult.getOrThrow().getId();

        AccessFilter result = accessFilterService.getById(filterId);

        assertNotNull(result);
        assertEquals(filterId, result.getId());
    }

    @Test
    void getById_withNonExistentId_returnsNull() {
        AccessFilter result = accessFilterService.getById("non-existent-id");

        assertNull(result);
    }
}