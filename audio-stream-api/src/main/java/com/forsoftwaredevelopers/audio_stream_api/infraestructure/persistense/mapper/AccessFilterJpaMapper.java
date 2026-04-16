package com.forsoftwaredevelopers.audio_stream_api.infraestructure.persistense.mapper;

import org.mapstruct.Mapper;

import com.forsoftwaredevelopers.audio_stream_api.domain.model.AccessFilter;
import com.forsoftwaredevelopers.audio_stream_api.domain.model.AccessFilterStatus;
import com.forsoftwaredevelopers.audio_stream_api.infraestructure.persistense.entity.AccessFilterJPAEntity;

@Mapper(componentModel = "spring")
public interface AccessFilterJpaMapper {
    
    default AccessFilterJPAEntity toJpaEntity(AccessFilter accessFilter) {
        var entity = new AccessFilterJPAEntity();
        entity.setId(accessFilter.getId());
        entity.setUsername(accessFilter.getUsername());
        entity.setEmail(accessFilter.getEmail());
        entity.setIp(accessFilter.getIp());
        entity.setStatus(accessFilter.getStatus().name());
        entity.setCreatedAt(accessFilter.getCreatedAt());
        entity.setUpdatedAt(accessFilter.getUpdatedAt());
        return entity;
    }
    
    default AccessFilter toDomainModel(AccessFilterJPAEntity entity) {
        return AccessFilter.restore(
            entity.getId(),
            entity.getUsername(),
            entity.getEmail(),
            entity.getIp(),
            AccessFilterStatus.valueOf(entity.getStatus()),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}