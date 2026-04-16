package com.forsoftwaredevelopers.audio_stream_api.domain.model;

import java.util.List;

public record TokenPayload(String sub, String type, List<String> scopes) {}