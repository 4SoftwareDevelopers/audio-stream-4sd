package com.forsoftwaredevelopers.audio_stream_api.infraestructure.persistense.repository;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.forsoftwaredevelopers.audio_stream_api.infraestructure.persistense.entity.RefreshTokenJPAEntity;

@Repository
public interface RefreshTokenJPARepository extends JpaRepository<RefreshTokenJPAEntity, String> {
    
    Optional<RefreshTokenJPAEntity> findByToken(String token);
    
    @Modifying
    @Query("UPDATE RefreshTokenJPAEntity r SET r.revoked = true WHERE r.userId = :userId AND r.userType = :userType")
    int revokeAllByUserIdAndType(String userId, String userType);
    
    @Modifying
    @Query("DELETE FROM RefreshTokenJPAEntity r WHERE r.expiryDate < :now")
    int deleteAllExpired(Instant now);
}