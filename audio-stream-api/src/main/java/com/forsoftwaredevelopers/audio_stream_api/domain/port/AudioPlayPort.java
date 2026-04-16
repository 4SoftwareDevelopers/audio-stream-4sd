package com.forsoftwaredevelopers.audio_stream_api.domain.port;

import com.forsoftwaredevelopers.audio_stream_api.domain.result.Result;

public interface AudioPlayPort {

    Result<Void> playAudio(String voiceMessageId, String storageKey);
}