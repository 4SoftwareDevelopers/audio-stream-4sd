package com.forsoftwaredevelopers.audio_stream_api.infraestructure.web.admin;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.forsoftwaredevelopers.audio_stream_api.domain.port.AudioStoragePort;
import com.forsoftwaredevelopers.audio_stream_api.domain.result.DomainError;
import com.forsoftwaredevelopers.audio_stream_api.domain.result.ErrorType;
import com.forsoftwaredevelopers.audio_stream_api.domain.result.Result;

@Service
public class AudioPlayService {

    private final SimpMessagingTemplate messagingTemplate;
    private final AudioStoragePort audioStoragePort;

    public AudioPlayService(SimpMessagingTemplate messagingTemplate, AudioStoragePort audioStoragePort) {
        this.messagingTemplate = messagingTemplate;
        this.audioStoragePort = audioStoragePort;
    }

    public Result<Void> playAudio(String voiceMessageId, String storageKey) {
        try {
            byte[] audioData = audioStoragePort.retrieve(storageKey);
            
            AudioPlayMessage message = new AudioPlayMessage(
                voiceMessageId,
                audioData,
                "audio/wav"
            );
            
            messagingTemplate.convertAndSend("/topic/audio", message);
            return Result.ok(null);
        } catch (Exception e) {
            return Result.fail(new DomainError("AUDIO_PLAY_ERROR", "Failed to play audio: " + e.getMessage(), ErrorType.INTERNAL));
        }
    }

    public record AudioPlayMessage(String voiceMessageId, byte[] audioData, String mimeType) {}
}