package com.forsoftwaredevelopers.audio_stream_api.application.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Service;

import com.forsoftwaredevelopers.audio_stream_api.application.command.MarkAsBlackListCommand;
import com.forsoftwaredevelopers.audio_stream_api.application.usecase.MarkAsBlackListUseCase;
import com.forsoftwaredevelopers.audio_stream_api.domain.model.AccessFilter;
import com.forsoftwaredevelopers.audio_stream_api.domain.model.AccessFilterStatus;
import com.forsoftwaredevelopers.audio_stream_api.domain.model.VoiceMessage;
import com.forsoftwaredevelopers.audio_stream_api.domain.port.AccessFilterRepository;
import com.forsoftwaredevelopers.audio_stream_api.domain.port.VoiceMessageRepository;
import com.forsoftwaredevelopers.audio_stream_api.domain.result.DomainError;
import com.forsoftwaredevelopers.audio_stream_api.domain.result.ErrorType;
import com.forsoftwaredevelopers.audio_stream_api.domain.result.Result;

@Service
public class MarkAsBlackListService implements MarkAsBlackListUseCase {

    public static final String VOICE_MESSAGE_NOT_FOUND = "VOICE_MESSAGE_NOT_FOUND";
    public static final String IP_BLOCKED = "IP_BLOCKED";

    private static final int MAX_MESSAGES_PER_STREAM = 3;

    private final AccessFilterRepository accessFilterRepository;
    private final VoiceMessageRepository voiceMessageRepository;

    public MarkAsBlackListService(AccessFilterRepository accessFilterRepository, VoiceMessageRepository voiceMessageRepository) {
        this.accessFilterRepository = accessFilterRepository;
        this.voiceMessageRepository = voiceMessageRepository;
    }

    @Override
    public Result<Void> markAsBlackList(MarkAsBlackListCommand command) {
        VoiceMessage voiceMessage = voiceMessageRepository.findById(command.voiceMessageId());
        
        if (voiceMessage == null) {
            return Result.fail(new DomainError(VOICE_MESSAGE_NOT_FOUND, "Voice message not found", ErrorType.NOT_FOUND));
        }

        int messageCount = accessFilterRepository.countByIpAndStatusInTimeWindow(
            command.ip(), 
            Instant.now().minus(24, ChronoUnit.HOURS)
        );

        if (messageCount >= MAX_MESSAGES_PER_STREAM) {
            var blockResult = AccessFilter.create(null, null, command.ip(), AccessFilterStatus.BLOCKED);
            if (blockResult.isFail()) {
                return blockResult.propagate();
            }
            accessFilterRepository.save(blockResult.getOrThrow());
            return Result.fail(new DomainError(IP_BLOCKED, "IP has reached the limit of messages per stream", ErrorType.CONFLICT));
        }

        var blackListResult = AccessFilter.create(
            command.username(),
            command.email(),
            command.ip(),
            AccessFilterStatus.BLACKLIST
        );
        
        if (blackListResult.isFail()) {
            return blackListResult.propagate();
        }

        accessFilterRepository.save(blackListResult.getOrThrow());
        return Result.ok(null);
    }
}