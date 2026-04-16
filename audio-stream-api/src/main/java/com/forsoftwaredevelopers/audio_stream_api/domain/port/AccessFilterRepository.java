package com.forsoftwaredevelopers.audio_stream_api.domain.port;

import java.time.Instant;
import java.util.List;

import com.forsoftwaredevelopers.audio_stream_api.domain.model.AccessFilter;

public interface AccessFilterRepository {
    AccessFilter save(AccessFilter accessFilter);
    AccessFilter findById(String id);
    void deleteById(String id);
    List<AccessFilter> findByIp(String ip);
    
    PagedResult<AccessFilter> findWithFilters(
        String username,
        String email,
        String ip,
        String status,
        Instant startDate,
        Instant endDate,
        int page,
        int size
    );
    
    int countByIpAndStatusInTimeWindow(String ip, Instant since);
    
    record PagedResult<T>(List<T> content, long totalElements, int totalPages) {}
}