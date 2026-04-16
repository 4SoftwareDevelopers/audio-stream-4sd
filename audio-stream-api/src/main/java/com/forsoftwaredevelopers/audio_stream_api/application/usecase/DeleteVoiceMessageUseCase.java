package com.forsoftwaredevelopers.audio_stream_api.application.usecase;

import com.forsoftwaredevelopers.audio_stream_api.application.command.DeleteVoiceMessageCommand;
import com.forsoftwaredevelopers.audio_stream_api.domain.result.Result;

public interface DeleteVoiceMessageUseCase {
    Result<Void> delete(DeleteVoiceMessageCommand command);
}