package com.forsoftwaredevelopers.audio_stream_api.domain.port;

import java.time.Instant;
import java.util.List;

import com.forsoftwaredevelopers.audio_stream_api.domain.model.VoiceMessage;

public interface VoiceMessageRepository {
    VoiceMessage save(VoiceMessage voiceMessage);
    List<VoiceMessage> findByStatus(String status);
    VoiceMessage findById(String id);
    void deleteById(String id);
    
    PagedResult<VoiceMessage> findWithFilters(
        String status, 
        String streamId, 
        String username, 
        Instant startDate, 
        Instant endDate, 
        int page, 
        int size
    );
    
    VoiceMessageAudioResult findAudioByVoiceMessageId(String voiceMessageId);
    
    void saveAudioMetadata(String voiceMessageId, String storageKey, String mimeType, long sizeBytes, int durationSeconds);
    
    record PagedResult<T>(List<T> content, long totalElements, int totalPages) {}
    
    record VoiceMessageAudioResult(String voiceMessageId, String storageKey, String mimeType) {}
}