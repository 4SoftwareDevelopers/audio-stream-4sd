package com.forsoftwaredevelopers.audio_stream_api.infraestructure.persistense.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.forsoftwaredevelopers.audio_stream_api.infraestructure.persistense.entity.VoiceMessageJPAEntity;

public interface VoiceMessageJPARepository extends JpaRepository<VoiceMessageJPAEntity, String> {
    List<VoiceMessageJPAEntity> findByStreamIdAndStatus(String streamId, String status);
    List<VoiceMessageJPAEntity> findByStatusOrderByCreatedAtDesc(String status);
    
    @Query("SELECT vm FROM VoiceMessageJPAEntity vm WHERE " +
           "(:status IS NULL OR vm.status = :status) AND " +
           "(:streamId IS NULL OR vm.streamId = :streamId) AND " +
           "(:username IS NULL OR vm.username LIKE %:username%) AND " +
           "(:startDate IS NULL OR vm.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR vm.createdAt <= :endDate)")
    Page<VoiceMessageJPAEntity> findWithFilters(
        @Param("status") String status,
        @Param("streamId") String streamId,
        @Param("username") String username,
        @Param("startDate") Instant startDate,
        @Param("endDate") Instant endDate,
        Pageable pageable
    );
}