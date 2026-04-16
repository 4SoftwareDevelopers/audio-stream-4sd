package com.forsoftwaredevelopers.audio_stream_api.infraestructure.persistense.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.forsoftwaredevelopers.audio_stream_api.infraestructure.persistense.entity.VoiceMessageAudioJPAEntity;

public interface VoiceMessageAudioJPARepository extends JpaRepository<VoiceMessageAudioJPAEntity, String> {
    Optional<VoiceMessageAudioJPAEntity> findByVoiceMessageId(String voiceMessageId);
}