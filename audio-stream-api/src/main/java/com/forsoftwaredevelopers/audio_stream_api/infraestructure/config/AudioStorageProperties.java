package com.forsoftwaredevelopers.audio_stream_api.infraestructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "audio.storage")
public record AudioStorageProperties(String path) {
}
