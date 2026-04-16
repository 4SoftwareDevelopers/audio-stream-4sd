package com.forsoftwaredevelopers.audio_stream_api.application.command;

import java.time.Instant;

public record ListAccessFilterCommand(
    String username,
    String email,
    String ip,
    String status,
    Instant startDate,
    Instant endDate,
    Integer page,
    Integer size
) {
    public static ListAccessFilterCommand of(
            String username, String email, String ip, String status,
            Instant startDate, Instant endDate, Integer page, Integer size) {
        return new ListAccessFilterCommand(
            username, email, ip, status, startDate, endDate,
            page != null ? page : 0,
            size != null ? size : 20
        );
    }
}