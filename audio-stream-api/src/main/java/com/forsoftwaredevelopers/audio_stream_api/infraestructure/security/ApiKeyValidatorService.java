package com.forsoftwaredevelopers.audio_stream_api.infraestructure.security;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.forsoftwaredevelopers.audio_stream_api.infraestructure.persistense.entity.ApiKeyJPAEntity;
import com.forsoftwaredevelopers.audio_stream_api.infraestructure.persistense.repository.ApiKeyJPARepository;

@Service
public class ApiKeyValidatorService {

    private final ApiKeyJPARepository apiKeyRepository;

    public ApiKeyValidatorService(ApiKeyJPARepository apiKeyRepository) {
        this.apiKeyRepository = apiKeyRepository;
    }

    public Optional<ApiKeyJPAEntity> validate(String apiKey, String apiSecret) {
        return apiKeyRepository.findByApiKey(apiKey)
                .filter(key -> key.isActivo())
                .filter(key -> key.getApiSecret().equals(apiSecret));
    }

    public record ApiKeyValidationResult(String id, String tipoCliente, List<String> permisos) {}

    public Optional<ApiKeyValidationResult> validateAndParse(String apiKey, String apiSecret) {
        return validate(apiKey, apiSecret)
                .map(key -> new ApiKeyValidationResult(
                        key.getId(),
                        key.getTipoCliente().name(),
                        parsePermisos(key.getPermisos())
                ));
    }

    private List<String> parsePermisos(String permisos) {
        if (permisos == null || permisos.isBlank()) {
            return List.of();
        }
        return List.of(permisos.split(","));
    }
}