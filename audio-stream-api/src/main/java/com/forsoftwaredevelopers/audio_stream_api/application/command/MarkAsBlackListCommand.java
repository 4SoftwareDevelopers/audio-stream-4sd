package com.forsoftwaredevelopers.audio_stream_api.application.command;

public record MarkAsBlackListCommand(
    String voiceMessageId,
    String ip,
    String username,
    String email
) {}