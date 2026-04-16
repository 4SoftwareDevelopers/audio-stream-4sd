package com.forsoftwaredevelopers.audio_stream_api.infraestructure.persistense.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.forsoftwaredevelopers.audio_stream_api.infraestructure.persistense.entity.ApiKeyJPAEntity;

@Repository
public interface ApiKeyJPARepository extends JpaRepository<ApiKeyJPAEntity, String> {
    Optional<ApiKeyJPAEntity> findByApiKey(String apiKey);
    boolean existsByApiKey(String apiKey);
}