package com.forsoftwaredevelopers.audio_stream_api.infraestructure.persistense.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.forsoftwaredevelopers.audio_stream_api.infraestructure.persistense.entity.AdminUserJPAEntity;

@Repository
public interface AdminUserJPARepository extends JpaRepository<AdminUserJPAEntity, String> {
    Optional<AdminUserJPAEntity> findByUsername(String username);
    boolean existsByUsername(String username);
}