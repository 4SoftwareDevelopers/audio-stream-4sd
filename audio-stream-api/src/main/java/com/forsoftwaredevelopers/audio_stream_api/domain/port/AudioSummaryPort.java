package com.forsoftwaredevelopers.audio_stream_api.domain.port;

import java.util.List;

public interface AudioSummaryPort {
    SummaryResult summarize(byte[] audioData);
    
    record SummaryResult(String summary, List<String> keywords) {}
}