package com.forsoftwaredevelopers.audio_stream_api.application.command;

public record AuthResponse(String jwt, String refreshToken, Long expiresIn) {}