package com.forsoftwaredevelopers.audio_stream_api.application.command;

import java.time.Instant;

public record ListPendingVoiceMessageCommand(
    String status,
    String streamId,
    String username,
    Instant startDate,
    Instant endDate,
    Integer page,
    Integer size
) {
    public static ListPendingVoiceMessageCommand of(
            String status,
            String streamId,
            String username,
            Instant startDate,
            Instant endDate,
            Integer page,
            Integer size) {
        return new ListPendingVoiceMessageCommand(
            status, streamId, username, startDate, endDate, 
            page != null ? page : 0, 
            size != null ? size : 20);
    }
}