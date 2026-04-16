package com.forsoftwaredevelopers.audio_stream_api.application.usecase;

import com.forsoftwaredevelopers.audio_stream_api.application.command.ListPendingVoiceMessageCommand;
import com.forsoftwaredevelopers.audio_stream_api.application.command.PagedResponse;
import com.forsoftwaredevelopers.audio_stream_api.domain.model.VoiceMessage;

public interface ListPendingVoiceMessageUseCase {
    PagedResponse<VoiceMessage> list(ListPendingVoiceMessageCommand command);
}