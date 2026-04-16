package com.forsoftwaredevelopers.audio_stream_api.application.usecase;

import java.util.List;

import com.forsoftwaredevelopers.audio_stream_api.application.command.ResumeVoiceMessageCommand;
import com.forsoftwaredevelopers.audio_stream_api.domain.result.Result;

public interface ResumeVoiceMessageUseCase {
    Result<ResumeResult> resume(ResumeVoiceMessageCommand command);
    
    record ResumeResult(String summary, List<String> keywords) {}
}