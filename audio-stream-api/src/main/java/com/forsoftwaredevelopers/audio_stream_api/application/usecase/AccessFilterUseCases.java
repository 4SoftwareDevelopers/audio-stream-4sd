package com.forsoftwaredevelopers.audio_stream_api.application.usecase;

import java.time.Instant;

import com.forsoftwaredevelopers.audio_stream_api.application.command.ListAccessFilterCommand;
import com.forsoftwaredevelopers.audio_stream_api.application.command.PagedResponse;
import com.forsoftwaredevelopers.audio_stream_api.domain.model.AccessFilter;
import com.forsoftwaredevelopers.audio_stream_api.domain.result.Result;

public interface AccessFilterUseCases {
    Result<AccessFilter> create(CreateAccessFilterCommand command);
    Result<AccessFilter> update(UpdateAccessFilterCommand command);
    Result<Void> delete(DeleteAccessFilterCommand command);
    PagedResponse<AccessFilter> list(ListAccessFilterCommand command);
    AccessFilter getById(String id);
    
    record CreateAccessFilterCommand(String username, String email, String ip, String status) {}
    record UpdateAccessFilterCommand(String id, String status) {}
    record DeleteAccessFilterCommand(String id) {}
}