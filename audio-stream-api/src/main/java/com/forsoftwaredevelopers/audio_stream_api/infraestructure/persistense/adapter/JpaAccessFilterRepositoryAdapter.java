package com.forsoftwaredevelopers.audio_stream_api.infraestructure.persistense.adapter;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import com.forsoftwaredevelopers.audio_stream_api.domain.model.AccessFilter;
import com.forsoftwaredevelopers.audio_stream_api.domain.port.AccessFilterRepository;
import com.forsoftwaredevelopers.audio_stream_api.infraestructure.persistense.entity.AccessFilterJPAEntity;
import com.forsoftwaredevelopers.audio_stream_api.infraestructure.persistense.mapper.AccessFilterJpaMapper;
import com.forsoftwaredevelopers.audio_stream_api.infraestructure.persistense.repository.AccessFilterJPARepository;

@Repository
public class JpaAccessFilterRepositoryAdapter implements AccessFilterRepository {

    private final AccessFilterJPARepository accessFilterJPARepository;
    private final AccessFilterJpaMapper accessFilterJpaMapper;

    public JpaAccessFilterRepositoryAdapter(AccessFilterJPARepository accessFilterJPARepository, AccessFilterJpaMapper accessFilterJpaMapper) {
        this.accessFilterJPARepository = accessFilterJPARepository;
        this.accessFilterJpaMapper = accessFilterJpaMapper;
    }

    @Override
    public AccessFilter save(AccessFilter accessFilter) {
        var entity = accessFilterJpaMapper.toJpaEntity(accessFilter);
        var savedEntity = accessFilterJPARepository.save(entity);
        return accessFilterJpaMapper.toDomainModel(savedEntity);
    }

    @Override
    public AccessFilter findById(String id) {
        return accessFilterJPARepository.findById(id)
            .map(accessFilterJpaMapper::toDomainModel)
            .orElse(null);
    }

    @Override
    public void deleteById(String id) {
        accessFilterJPARepository.deleteById(id);
    }

    @Override
    public List<AccessFilter> findByIp(String ip) {
        return accessFilterJPARepository.findByIp(ip).stream()
            .map(accessFilterJpaMapper::toDomainModel)
            .collect(Collectors.toList());
    }

    @Override
    public PagedResult<AccessFilter> findWithFilters(
            String username,
            String email,
            String ip,
            String status,
            Instant startDate,
            Instant endDate,
            int page,
            int size) {
        
        Page<AccessFilterJPAEntity> result = accessFilterJPARepository.findWithFilters(
            username, email, ip, status, startDate, endDate, PageRequest.of(page, size));
        
        List<AccessFilter> content = result.getContent().stream()
            .map(accessFilterJpaMapper::toDomainModel)
            .collect(Collectors.toList());
        
        return new PagedResult<>(content, result.getTotalElements(), result.getTotalPages());
    }

    @Override
    public int countByIpAndStatusInTimeWindow(String ip, Instant since) {
        List<String> statuses = List.of("WHITELIST", "BLACKLIST");
        return accessFilterJPARepository.countByIpAndStatusInTimeWindow(ip, statuses, since);
    }
}