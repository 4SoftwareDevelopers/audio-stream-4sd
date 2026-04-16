package com.forsoftwaredevelopers.audio_stream_api.domain.port;

import com.forsoftwaredevelopers.audio_stream_api.domain.result.Result;

public interface AudioValidationPort {
    Result<AudioValidationResult> validate(byte[] audioData);
    
    record AudioValidationResult(
        int durationSeconds, 
        long sizeBytes, 
        String format
    ) {}
}