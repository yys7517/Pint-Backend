# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

```bash
# Build
./gradlew build

# Run locally (requires .env file — see Environment Variables below)
./gradlew bootRun

# Run tests
./gradlew test

# Run a single test class
./gradlew test --tests "com.example.pintbackend.ClassName"

# Start local infrastructure (PostgreSQL + Redis)
docker-compose up -d postgres redis

# Start full stack (app + infrastructure)
docker-compose up -d --build
```

## Environment Variables

The app reads config from a `.env` file in the project root (loaded via `dotenv-java` and `spring.config.import: optional:file:.env[.properties]`). Required variables:

| Variable | Description |
|---|---|
| `DB_HOST_NAME` | PostgreSQL host (e.g. `localhost`) |
| `PSQL_DB` | Database name |
| `DB_USER` | DB username |
| `DB_PWD` | DB password |
| `REDIS_HOST_NAME` | Redis host (also used as Docker container name) |
| `AWS_REGION` | AWS region (e.g. `ap-northeast-2`) |
| `AWS_S3_BUCKET` | S3 bucket name |

AWS credentials are loaded automatically from IAM (EC2 instance profile or `~/.aws/credentials`).

## Architecture Overview

Spring Boot 3.5 / Java 17 / Gradle project. PostgreSQL for persistence, Redis configured (not yet active in business logic), AWS S3 for file storage.

### Package Structure

```
com.example.pintbackend
├── aop/            # Cross-cutting concerns (ApiLoggingAspect — measures controller execution time)
├── config/         # Spring config beans (S3Config, WebConfig)
├── controller/     # REST layer — thin, delegates to services
├── domain/         # JPA entities (all extend BaseEntity for createdAt/updatedAt)
├── dto/            # Request/Response DTOs
│   ├── common/response/BaseResponse.java  # Universal API envelope: {code, message, data}
│   └── postDto/    # Post-specific DTOs
├── repository/     # Spring Data JPA repositories
└── service/
    ├── PostService.java          # Post CRUD business logic
    └── s3service/
        ├── S3Service.java        # S3 upload/download (presigned URLs) / delete
        └── XmpAnalysisService.java  # Parses Adobe XMP filter files into structured JSON
```

### Core Data Flow

**Post creation** (`POST /posts`, multipart/form-data):
1. Upload image + XMP filter file to S3 → get back S3 keys
2. Save `Post` entity with S3 keys (not URLs) in DB

**Post retrieval** (`GET /posts/{id}`):
1. Load `Post` from DB
2. Generate presigned S3 URL (1-hour TTL) for the image
3. Fetch the XMP file from S3, parse it → `XmpAnalysisResponse` (basic/color/detail filter settings)

**XMP Analysis**: `XmpAnalysisService` parses Adobe Camera Raw XMP XML, extracts `crs:*` namespace attributes, strips default/zero values, and groups into `basic`, `color`, and `detail` categories.

### API Response Format

All endpoints return `BaseResponse<T>`:
```json
{ "code": 200, "message": "Success", "data": { ... } }
```

### S3 Key Convention

- Images: `images/<UUID>.<ext>`
- XMP filter files: `xmp/<UUID>.xmp`

S3 keys (not full URLs) are stored in the DB. Presigned URLs are generated on-demand at read time.

### Deployment

Push to `main` triggers GitHub Actions CI which SSHs into an EC2 instance, pulls latest, runs `./gradlew build`, and restarts via `docker-compose up -d --build`.

Swagger UI available at `/swagger-ui/index.html` when running.
