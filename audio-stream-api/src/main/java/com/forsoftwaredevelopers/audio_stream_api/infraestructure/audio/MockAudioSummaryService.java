package com.forsoftwaredevelopers.audio_stream_api.infraestructure.audio;

import java.util.List;

import org.springframework.stereotype.Component;

import com.forsoftwaredevelopers.audio_stream_api.domain.port.AudioSummaryPort;
import com.forsoftwaredevelopers.audio_stream_api.domain.port.AudioSummaryPort.SummaryResult;

@Component
public class MockAudioSummaryService implements AudioSummaryPort {

    @Override
    public SummaryResult summarize(byte[] audioData) {
        return new SummaryResult(
            "This is a mock summary. Integrate with an AI service like OpenAI Whisper for production.",
            List.of("mock", "placeholder", "ai-service-needed")
        );
    }
}