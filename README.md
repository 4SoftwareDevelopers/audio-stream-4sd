# Audio Stream

Plataforma de streaming de audio en vivo con panel de administración.

## Estructura del Proyecto

```
audio-stream-4sd/
├── audio-stream-api/          # Backend API (Spring Boot)
├── audio-stream-web-admin/    # Panel de administración (Angular)
└── docs/                      # Documentación del proyecto
```

---

## audio-stream-api

API REST Backend con arquitectura hexagonal.

### Tecnologías
- **Java 21** + Spring Boot 4.0.0
- **Maven** (wrapper incluido)
- **PostgreSQL** + Flyway migrations
- **OAuth2 Resource Server** (JWT)

### Comandos

```bash
cd audio-stream-api

# Compilar
./mvnw clean package

# Ejecutar
./mvnw spring-boot:run

# Tests
./mvnw test
```

### Arquitectura
- Arquitectura Hexagonal (Domain-Driven Design)
- Migraciones en `src/main/resources/db/migration/`
- Documentación en `docs/specs/` y `AGENTS.md`

---

## audio-stream-web-admin

Panel de administración Angular.

### Tecnologías
- **Angular 21** (standalone components)
- **PrimeNG** (componentes UI)
- **TypeScript**

### Comandos

```bash
cd audio-stream-web-admin

# Desarrollo
npm start          # http://localhost:4200

# Build producción
npm run build
```

---

## Requisitos Previos

- Java 21+
- Node.js 18+
- PostgreSQL (para API)
- Maven (incluido en el proyecto)

---

## Variables de Entorno

### API (`audio-stream-api/src/main/resources/application.properties`)
```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/audio_stream
spring.datasource.username=postgres
spring.datasource.password=your_password

# JWT
app.jwt.secret=your_jwt_secret_key
```

---

## Documentación

- `audio-stream-api/AGENTS.md` - Guía de desarrollo del API
- `audio-stream-api/HELP.md` - Comandos útiles
- `docs/specs/` - Especificaciones de funcionalidades