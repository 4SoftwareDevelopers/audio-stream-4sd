package com.forsoftwaredevelopers.audio_stream_api.infraestructure.persistense.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import com.forsoftwaredevelopers.audio_stream_api.domain.model.VoiceMessage;
import com.forsoftwaredevelopers.audio_stream_api.domain.model.VoiceMessageStatus;
import com.forsoftwaredevelopers.audio_stream_api.infraestructure.persistense.entity.VoiceMessageJPAEntity;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface VoiceMessageJpaMapper {

    @Named("stringToUUID")
    default UUID stringToUUID(String id) {
        return id != null ? UUID.fromString(id) : null;
    }

    @Mapping(target = "status", expression = "java(voiceMessage.getStatus().name())", ignore = true)
    @Mapping(target = "id", source = "id", qualifiedByName = "stringToUUID")
    VoiceMessageJPAEntity toJpaEntity(VoiceMessage voiceMessage);


    default VoiceMessage toDomainModel(VoiceMessageJPAEntity voiceMessageJPAEntity) {
        return VoiceMessage.restore(
            voiceMessageJPAEntity.getId().toString(),
            voiceMessageJPAEntity.getStreamId(),
            voiceMessageJPAEntity.getUsername(),
            voiceMessageJPAEntity.getEmail(),
            VoiceMessageStatus.fromString(voiceMessageJPAEntity.getStatus()),
            voiceMessageJPAEntity.getCreatedAt(),
            voiceMessageJPAEntity.getUpdatedAt()
        );
    }
}
