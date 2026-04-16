package com.forsoftwaredevelopers.audio_stream_api.infraestructure.audio;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.forsoftwaredevelopers.audio_stream_api.domain.port.AudioStoragePort;
import com.forsoftwaredevelopers.audio_stream_api.infraestructure.config.AudioStorageProperties;

import jakarta.annotation.PostConstruct;

@Component
public class FileSystemAudioStorage implements AudioStoragePort {

    private final AudioStorageProperties audioStorageProperties;

    public FileSystemAudioStorage(AudioStorageProperties audioStorageProperties) {
        this.audioStorageProperties = audioStorageProperties;
    }

    @PostConstruct
    public void init() {
        try {
            Path storagePath = Path.of(audioStorageProperties.path());
            if (!Files.exists(storagePath)) {
                Files.createDirectories(storagePath);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to create audio storage directory", e);
        }
    }
    
    @Override
    public String store(byte[] audio) {
        String fileName = UUID.randomUUID().toString() + ".wav";
        Path path = Path.of(audioStorageProperties.path(), fileName);
        try {
            Files.write(path, audio);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store audio", e);
        }
        return path.toString();
    }

    @Override
    public byte[] retrieve(String audioPath) {
        try {
            return Files.readAllBytes(Path.of(audioPath));
        } catch (IOException e) {
            throw new RuntimeException("Failed to retrieve audio", e);
        }
    }
}