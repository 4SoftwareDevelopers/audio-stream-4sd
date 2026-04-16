package com.forsoftwaredevelopers.audio_stream_api.application.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.forsoftwaredevelopers.audio_stream_api.application.command.DeleteVoiceMessageCommand;
import com.forsoftwaredevelopers.audio_stream_api.application.command.ListPendingVoiceMessageCommand;
import com.forsoftwaredevelopers.audio_stream_api.application.command.PagedResponse;
import com.forsoftwaredevelopers.audio_stream_api.application.command.PlayVoiceMessageCommand;
import com.forsoftwaredevelopers.audio_stream_api.application.command.ReceiveVoiceMessageCommand;
import com.forsoftwaredevelopers.audio_stream_api.application.command.RejectVoiceMessageCommand;
import com.forsoftwaredevelopers.audio_stream_api.application.usecase.DeleteVoiceMessageUseCase;
import com.forsoftwaredevelopers.audio_stream_api.application.usecase.ListPendingVoiceMessageUseCase;
import com.forsoftwaredevelopers.audio_stream_api.application.usecase.PlayVoiceMessageUseCase;
import com.forsoftwaredevelopers.audio_stream_api.application.usecase.ReceiveVoiceMessageUseCase;
import com.forsoftwaredevelopers.audio_stream_api.application.usecase.RejectVoiceMessageUseCase;
import com.forsoftwaredevelopers.audio_stream_api.domain.model.VoiceMessage;
import com.forsoftwaredevelopers.audio_stream_api.domain.port.AudioPlayPort;
import com.forsoftwaredevelopers.audio_stream_api.domain.port.AudioStoragePort;
import com.forsoftwaredevelopers.audio_stream_api.domain.port.AudioValidationPort;
import com.forsoftwaredevelopers.audio_stream_api.domain.port.VoiceMessageRepository;
import com.forsoftwaredevelopers.audio_stream_api.domain.result.DomainError;
import com.forsoftwaredevelopers.audio_stream_api.domain.result.ErrorType;
import com.forsoftwaredevelopers.audio_stream_api.domain.result.Result;

@Service
public class VoiceMessageService implements ReceiveVoiceMessageUseCase, RejectVoiceMessageUseCase, 
        ListPendingVoiceMessageUseCase, PlayVoiceMessageUseCase, DeleteVoiceMessageUseCase {

    public static final String MESSAGE_NOT_FOUND = "MESSAGE_NOT_FOUND";
    public static final String AUDIO_TOO_LARGE = "AUDIO_TOO_LARGE";
    public static final long MAX_AUDIO_SIZE_BYTES = 1024 * 1024;  // 1MB

    private final VoiceMessageRepository voiceMessageRepository;
    private final AudioValidationPort audioValidationPort;
    private final AudioStoragePort audioStoragePort;
    private final AudioPlayPort audioPlayPort;

    public VoiceMessageService(
            VoiceMessageRepository voiceMessageRepository,
            AudioValidationPort audioValidationPort,
            AudioStoragePort audioStoragePort,
            AudioPlayPort audioPlayPort) {
        this.voiceMessageRepository = voiceMessageRepository;
        this.audioValidationPort = audioValidationPort;
        this.audioStoragePort = audioStoragePort;
        this.audioPlayPort = audioPlayPort;
    }

    @Override
    public Result<Void> play(PlayVoiceMessageCommand command) {
        VoiceMessage voiceMessage = voiceMessageRepository.findById(command.voiceMessageId());

        if (voiceMessage == null) {
            return Result.fail(new DomainError(MESSAGE_NOT_FOUND, "Voice message not found", ErrorType.NOT_FOUND));
        }

        var audioResult = voiceMessageRepository.findAudioByVoiceMessageId(command.voiceMessageId());
        if (audioResult != null) {
            audioPlayPort.playAudio(command.voiceMessageId(), audioResult.storageKey());
        }

        Result<Void> result = voiceMessage.markAsPlayed();
        if (result.isFail()) {
            return result.propagate();
        }
        voiceMessageRepository.save(voiceMessage);

        return Result.ok(null);
    }

    @Override
    public PagedResponse<VoiceMessage> list(ListPendingVoiceMessageCommand command) {
        var pagedResult = voiceMessageRepository.findWithFilters(
            command.status(),
            command.streamId(),
            command.username(),
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
    public Result<Void> reject(RejectVoiceMessageCommand command) {
        VoiceMessage voiceMessage = voiceMessageRepository.findById(command.voiceMessageId());
        
        if (voiceMessage == null) {
            return Result.fail(new DomainError(MESSAGE_NOT_FOUND, "Voice message not found", ErrorType.NOT_FOUND));
        }
        
        Result<Void> result = voiceMessage.markAsRejected();
        if (result.isFail()) {
            return result.propagate();
        }

        voiceMessageRepository.save(voiceMessage);
        return Result.ok(null);
    }

    @Override
    public Result<String> recieve(ReceiveVoiceMessageCommand command) {
        if (command.audio() == null || command.audio().length == 0) {
            return Result.fail(new DomainError("AUDIO_REQUIRED", "Audio data is required", ErrorType.VALIDATION));
        }

        if (command.audio().length > MAX_AUDIO_SIZE_BYTES) {
            return Result.fail(new DomainError(AUDIO_TOO_LARGE, "Audio exceeds 1MB", ErrorType.VALIDATION));
        }

        var validationResult = audioValidationPort.validate(command.audio());
        if (validationResult.isFail()) {
            return validationResult.propagate();
        }

        var audioValidation = validationResult.getOrThrow();

        var voiceMessageResult = VoiceMessage.create(command.streamId(), command.username(), command.email());
        if (voiceMessageResult.isFail()) {
            return voiceMessageResult.propagate();
        }

        VoiceMessage voiceMessage = voiceMessageResult.getOrThrow();
        voiceMessageRepository.save(voiceMessage);

        String storageKey = audioStoragePort.store(command.audio());
        
        String mimeType = "WAV".equals(audioValidation.format()) ? "audio/wav" : "audio/mpeg";
        voiceMessageRepository.saveAudioMetadata(
            voiceMessage.getId(),
            storageKey,
            mimeType,
            audioValidation.sizeBytes(),
            audioValidation.durationSeconds()
        );

        return Result.ok(voiceMessage.getId());
    }

    @Override
    public Result<Void> delete(DeleteVoiceMessageCommand command) {
        VoiceMessage voiceMessage = voiceMessageRepository.findById(command.voiceMessageId());
        
        if (voiceMessage == null) {
            return Result.fail(new DomainError(MESSAGE_NOT_FOUND, "Voice message not found", ErrorType.NOT_FOUND));
        }
        
        voiceMessageRepository.deleteById(command.voiceMessageId());
        return Result.ok(null);
    }
    
}