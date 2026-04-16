package com.forsoftwaredevelopers.audio_stream_api.application.usecase;

import com.forsoftwaredevelopers.audio_stream_api.application.command.MarkAsBlackListCommand;
import com.forsoftwaredevelopers.audio_stream_api.domain.result.Result;

public interface MarkAsBlackListUseCase {
    Result<Void> markAsBlackList(MarkAsBlackListCommand command);
}