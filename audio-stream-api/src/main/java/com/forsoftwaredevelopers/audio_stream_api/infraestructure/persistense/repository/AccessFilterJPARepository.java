package com.forsoftwaredevelopers.audio_stream_api.infraestructure.persistense.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.forsoftwaredevelopers.audio_stream_api.infraestructure.persistense.entity.AccessFilterJPAEntity;

public interface AccessFilterJPARepository extends JpaRepository<AccessFilterJPAEntity, String> {
    
    List<AccessFilterJPAEntity> findByIp(String ip);
    
    @Query("SELECT COUNT(af) FROM AccessFilterJPAEntity af WHERE af.ip = :ip AND af.status IN :statuses AND af.createdAt >= :since")
    int countByIpAndStatusInTimeWindow(
        @Param("ip") String ip,
        @Param("statuses") List<String> statuses,
        @Param("since") Instant since
    );
    
    @Query("SELECT af FROM AccessFilterJPAEntity af WHERE " +
           "(:username IS NULL OR af.username LIKE %:username%) AND " +
           "(:email IS NULL OR af.email LIKE %:email%) AND " +
           "(:ip IS NULL OR af.ip = :ip) AND " +
           "(:status IS NULL OR af.status = :status) AND " +
           "(:startDate IS NULL OR af.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR af.createdAt <= :endDate)")
    Page<AccessFilterJPAEntity> findWithFilters(
        @Param("username") String username,
        @Param("email") String email,
        @Param("ip") String ip,
        @Param("status") String status,
        @Param("startDate") Instant startDate,
        @Param("endDate") Instant endDate,
        Pageable pageable
    );
}