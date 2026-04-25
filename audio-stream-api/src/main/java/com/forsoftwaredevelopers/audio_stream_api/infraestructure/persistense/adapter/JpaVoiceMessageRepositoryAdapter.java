package com.forsoftwaredevelopers.audio_stream_api.infraestructure.persistense.adapter;

import com.forsoftwaredevelopers.audio_stream_api.domain.model.VoiceMessage;
import com.forsoftwaredevelopers.audio_stream_api.domain.port.VoiceMessageRepository;
import com.forsoftwaredevelopers.audio_stream_api.infraestructure.persistense.entity.VoiceMessageAudioJPAEntity;
import com.forsoftwaredevelopers.audio_stream_api.infraestructure.persistense.entity.VoiceMessageJPAEntity;
import com.forsoftwaredevelopers.audio_stream_api.infraestructure.persistense.mapper.VoiceMessageJpaMapper;
import com.forsoftwaredevelopers.audio_stream_api.infraestructure.persistense.repository.VoiceMessageAudioJPARepository;
import com.forsoftwaredevelopers.audio_stream_api.infraestructure.persistense.repository.VoiceMessageJPARepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class JpaVoiceMessageRepositoryAdapter implements VoiceMessageRepository {

    private final VoiceMessageJPARepository voiceMessageJPARepository;
    private final VoiceMessageAudioJPARepository voiceMessageAudioJPARepository;
    private final VoiceMessageJpaMapper voiceMessageJpaMapper;

    public JpaVoiceMessageRepositoryAdapter(
            VoiceMessageJPARepository voiceMessageJPARepository,
            VoiceMessageAudioJPARepository voiceMessageAudioJPARepository,
            VoiceMessageJpaMapper voiceMessageJpaMapper) {
        this.voiceMessageJPARepository = voiceMessageJPARepository;
        this.voiceMessageAudioJPARepository = voiceMessageAudioJPARepository;
        this.voiceMessageJpaMapper = voiceMessageJpaMapper;
    }

    @Override
    public VoiceMessage save(VoiceMessage voiceMessage) {
        var entity = voiceMessageJpaMapper.toJpaEntity(voiceMessage);
        var savedEntity = voiceMessageJPARepository.save(entity);
        return voiceMessageJpaMapper.toDomainModel(savedEntity);
    }

    @Override
    public List<VoiceMessage> findByStatus(String status) {
        var entities = voiceMessageJPARepository.findByStatusOrderByCreatedAtDesc(status);
        return entities.stream().map(voiceMessageJpaMapper::toDomainModel).collect(Collectors.toList());
    }

    @Override
    public VoiceMessage findById(String id) {
        try {
            var entity = voiceMessageJPARepository.findById(UUID.fromString(id));
            return entity.map(voiceMessageJpaMapper::toDomainModel).orElse(null);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public void deleteById(String id) {
        try {
            voiceMessageJPARepository.deleteById(UUID.fromString(id));
        } catch (IllegalArgumentException e) {
        }
    }

    @Override
    public PagedResult<VoiceMessage> findWithFilters(
            String status,
            String streamId,
            String username,
            Instant startDate,
            Instant endDate,
            int page,
            int size) {
        
        Page<VoiceMessageJPAEntity> result = voiceMessageJPARepository.findWithFilters(
            status, streamId, username, startDate, endDate, PageRequest.of(page, size));
        
        List<VoiceMessage> content = result.getContent().stream()
            .map(voiceMessageJpaMapper::toDomainModel)
            .collect(Collectors.toList());
        
        return new PagedResult<>(content, result.getTotalElements(), result.getTotalPages());
    }

    @Override
    public VoiceMessageAudioResult findAudioByVoiceMessageId(String voiceMessageId) {
        Optional<VoiceMessageAudioJPAEntity> audioEntity = voiceMessageAudioJPARepository.findByVoiceMessageId(voiceMessageId);
        
        return audioEntity.map(audio -> new VoiceMessageAudioResult(
            audio.getVoiceMessageId(),
            audio.getStorageKey(),
            audio.getMimeType()
        )).orElse(null);
    }

    @Override
    public void saveAudioMetadata(String voiceMessageId, String storageKey, String mimeType, long sizeBytes, int durationSeconds) {
        var entity = new VoiceMessageAudioJPAEntity();
        entity.setVoiceMessageId(voiceMessageId);
        entity.setStorageKey(storageKey);
        entity.setMimeType(mimeType);
        entity.setSizeBytes(sizeBytes);
        entity.setDuration(durationSeconds);
        entity.setCreatedAt(java.time.Instant.now());
        voiceMessageAudioJPARepository.save(entity);
    }
}