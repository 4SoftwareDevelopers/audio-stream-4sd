package com.forsoftwaredevelopers.audio_stream_api.application.command;

import java.util.List;

public record PagedResponse<T>(
    List<T> content,
    long totalElements,
    int totalPages,
    int page,
    int size
) {}