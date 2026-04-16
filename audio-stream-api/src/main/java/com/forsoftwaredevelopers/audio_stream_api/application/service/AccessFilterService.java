package com.forsoftwaredevelopers.audio_stream_api.application.service;

import java.time.Instant;

import org.springframework.stereotype.Service;

import com.forsoftwaredevelopers.audio_stream_api.application.command.ListAccessFilterCommand;
import com.forsoftwaredevelopers.audio_stream_api.application.command.PagedResponse;
import com.forsoftwaredevelopers.audio_stream_api.application.usecase.AccessFilterUseCases;
import com.forsoftwaredevelopers.audio_stream_api.domain.model.AccessFilter;
import com.forsoftwaredevelopers.audio_stream_api.domain.model.AccessFilterStatus;
import com.forsoftwaredevelopers.audio_stream_api.domain.port.AccessFilterRepository;
import com.forsoftwaredevelopers.audio_stream_api.domain.result.DomainError;
import com.forsoftwaredevelopers.audio_stream_api.domain.result.ErrorType;
import com.forsoftwaredevelopers.audio_stream_api.domain.result.Result;

@Service
public class AccessFilterService implements AccessFilterUseCases {

    public static final String FILTER_NOT_FOUND = "FILTER_NOT_FOUND";
    public static final String INVALID_STATUS = "INVALID_STATUS";

    private final AccessFilterRepository accessFilterRepository;

    public AccessFilterService(AccessFilterRepository accessFilterRepository) {
        this.accessFilterRepository = accessFilterRepository;
    }

    @Override
    public Result<AccessFilter> create(CreateAccessFilterCommand command) {
        AccessFilterStatus status;
        try {
            status = AccessFilterStatus.valueOf(command.status());
        } catch (IllegalArgumentException e) {
            return Result.fail(new DomainError(INVALID_STATUS, "Invalid status value: " + command.status(), ErrorType.VALIDATION));
        }
        
        var result = AccessFilter.create(command.username(), command.email(), command.ip(), status);
        
        if (result.isFail()) {
            return result.propagate();
        }
        
        AccessFilter saved = accessFilterRepository.save(result.getOrThrow());
        return Result.ok(saved);
    }

    @Override
    public Result<AccessFilter> update(UpdateAccessFilterCommand command) {
        AccessFilter accessFilter = accessFilterRepository.findById(command.id());
        
        if (accessFilter == null) {
            return Result.fail(new DomainError(FILTER_NOT_FOUND, "Access filter not found", ErrorType.NOT_FOUND));
        }
        
        AccessFilterStatus newStatus;
        try {
            newStatus = AccessFilterStatus.valueOf(command.status());
        } catch (IllegalArgumentException e) {
            return Result.fail(new DomainError(INVALID_STATUS, "Invalid status value", ErrorType.VALIDATION));
        }
        
        var updateResult = accessFilter.updateStatus(newStatus);
        if (updateResult.isFail()) {
            return updateResult.propagate();
        }
        
        AccessFilter saved = accessFilterRepository.save(accessFilter);
        return Result.ok(saved);
    }

    @Override
    public Result<Void> delete(DeleteAccessFilterCommand command) {
        AccessFilter accessFilter = accessFilterRepository.findById(command.id());
        
        if (accessFilter == null) {
            return Result.fail(new DomainError(FILTER_NOT_FOUND, "Access filter not found", ErrorType.NOT_FOUND));
        }
        
        accessFilterRepository.deleteById(command.id());
        return Result.ok(null);
    }

    @Override
    public PagedResponse<AccessFilter> list(ListAccessFilterCommand command) {
        var pagedResult = accessFilterRepository.findWithFilters(
            command.username(),
            command.email(),
            command.ip(),
            command.status(),
            command.startDate(),
            command.endDate(),
            command.page(),
            command.size()
        );
        
        return new PagedResponse<>(
            pagedResult.content(),
            pagedResult.totalElements(),
            pagedResult.totalPages(),
            command.page(),
            command.size()
        );
    }

    @Override
    public AccessFilter getById(String id) {
        return accessFilterRepository.findById(id);
    }
}