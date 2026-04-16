package com.forsoftwaredevelopers.audio_stream_api.infraestructure.persistense.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "api_key")
public class ApiKeyJPAEntity {

    @Id
    @Column(length = 36)
    private String id;

    @Column(unique = true, nullable = false, length = 64)
    private String apiKey;

    @Column(nullable = false, length = 128)
    private String apiSecret;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApiKeyTipoCliente tipoCliente;

    @Column(length = 255)
    private String permisos;

    @Column(nullable = false)
    private boolean activo;

    @Column(nullable = false)
    private Instant createdAt;

    @Column
    private Instant updatedAt;

    public enum ApiKeyTipoCliente {
        USER,
        OBSERVER
    }

    public ApiKeyJPAEntity() {}

    public ApiKeyJPAEntity(String id, String apiKey, String apiSecret, ApiKeyTipoCliente tipoCliente, String permisos, boolean activo) {
        this.id = id;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.tipoCliente = tipoCliente;
        this.permisos = permisos;
        this.activo = activo;
        this.createdAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public String getApiSecret() { return apiSecret; }
    public void setApiSecret(String apiSecret) { this.apiSecret = apiSecret; }
    public ApiKeyTipoCliente getTipoCliente() { return tipoCliente; }
    public void setTipoCliente(ApiKeyTipoCliente tipoCliente) { this.tipoCliente = tipoCliente; }
    public String getPermisos() { return permisos; }
    public void setPermisos(String permisos) { this.permisos = permisos; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}