package com.forsoftwaredevelopers.audio_stream_api.application.service;

import org.springframework.stereotype.Service;

import com.forsoftwaredevelopers.audio_stream_api.application.command.ResumeVoiceMessageCommand;
import com.forsoftwaredevelopers.audio_stream_api.application.usecase.ResumeVoiceMessageUseCase;
import com.forsoftwaredevelopers.audio_stream_api.domain.port.AudioStoragePort;
import com.forsoftwaredevelopers.audio_stream_api.domain.port.AudioSummaryPort;
import com.forsoftwaredevelopers.audio_stream_api.domain.port.VoiceMessageRepository;
import com.forsoftwaredevelopers.audio_stream_api.domain.result.DomainError;
import com.forsoftwaredevelopers.audio_stream_api.domain.result.ErrorType;
import com.forsoftwaredevelopers.audio_stream_api.domain.result.Result;

@Service
public class VoiceMessageSummaryService implements ResumeVoiceMessageUseCase {

    public static final String AUDIO_NOT_FOUND = "AUDIO_NOT_FOUND";

    private final VoiceMessageRepository voiceMessageRepository;
    private final AudioStoragePort audioStoragePort;
    private final AudioSummaryPort audioSummaryPort;

    public VoiceMessageSummaryService(
            VoiceMessageRepository voiceMessageRepository,
            AudioStoragePort audioStoragePort,
            AudioSummaryPort audioSummaryPort) {
        this.voiceMessageRepository = voiceMessageRepository;
        this.audioStoragePort = audioStoragePort;
        this.audioSummaryPort = audioSummaryPort;
    }

    @Override
    public Result<ResumeResult> resume(ResumeVoiceMessageCommand command) {
        var audioResult = voiceMessageRepository.findAudioByVoiceMessageId(command.voiceMessageId());
        
        if (audioResult == null) {
            return Result.fail(new DomainError(AUDIO_NOT_FOUND, "Audio not found for voice message", ErrorType.NOT_FOUND));
        }

        byte[] audioData = audioStoragePort.retrieve(audioResult.storageKey());
        var summaryResult = audioSummaryPort.summarize(audioData);

        return Result.ok(new ResumeResult(summaryResult.summary(), summaryResult.keywords()));
    }
}