-- V4: Create admin_user table
CREATE TABLE admin_user (
    id VARCHAR(36) PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_admin_user_username ON admin_user(username);

-- V5: Create api_key table
CREATE TABLE api_key (
    id VARCHAR(36) PRIMARY KEY,
    api_key VARCHAR(64) UNIQUE NOT NULL,
    api_secret VARCHAR(128) NOT NULL,
    tipo_cliente VARCHAR(20) NOT NULL,
    permisos VARCHAR(255),
    activo BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_api_key_key ON api_key(api_key);

-- V6: Create refresh_token table
CREATE TABLE refresh_token (
    id VARCHAR(36) PRIMARY KEY,
    token VARCHAR(512) UNIQUE NOT NULL,
    user_id VARCHAR(36),
    user_type VARCHAR(20),
    expiry_date TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_refresh_token_token ON refresh_token(token);
CREATE INDEX idx_refresh_token_user ON refresh_token(user_id, user_type);
CREATE INDEX idx_refresh_token_expiry ON refresh_token(expiry_date);