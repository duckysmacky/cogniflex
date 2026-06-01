# CLAUDE.md

This file provides guidance to AI agents when working with code in this repository.

## Project Overview

Cogniflex is an AI content detection platform that analyzes text, images, and videos to determine whether they are AI-generated. It uses a dual-path analysis approach: **static analysis** (heuristic/pattern-based rules) and **dynamic analysis** (ML model inference via gRPC).

## Build & Run Commands

### Backend (Java 25 / Spring Boot 4.0.2)
```bash
cd backend
./gradlew build       # compile + test
./gradlew test        # run tests only
./gradlew bootRun     # run dev server
```

### Frontend (React 19 / TypeScript)
```bash
cd frontend
npm run dev           # dev server
npm run build         # tsc -b && vite build
eslint .              # lint
eslint . --fix        # lint + autofix
prettier --write .    # format
```

### ML Service (Python)
```bash
cd ml-service
pip install -r requirements.txt
python main.py        # start gRPC server on :50051
python tests/test_text_client.py    # manual test
```

### Full Stack
```bash
docker compose up --build           # all services
docker compose up --build ml-service  # individual service
```

## Architecture

Three-tier microservice architecture:

```
Frontend/Extension (React+TS)
        │ REST
        ▼
Backend (Spring Boot) ─── gRPC ──► ML Service (Python, :50051)
        │
   PostgreSQL 18 + Redis 8
```

### Backend Package Layout

All backend code lives under `io.github.duckysmacky.cogniflex`:

- **`analysis/`** — core detection pipeline
  - `Analyzer<R>` — top-level interface; `AnalysisOrchestrator` runs static + dynamic analyzers in parallel via `CompletableFuture`
  - `static_/` — rule-based heuristic analysis; `StaticAnalyzer` (abstract) evaluates a list of `AnalysisRule<C>` implementations and collects `Evidence`
  - `dynamic/` — ML model inference; `DynamicAnalyzer` delegates to `MLGrpcClient` which calls the Python gRPC service
  - `score/` — `ScoreFusionStrategy` merges static evidence + dynamic ML score into a `FinalScore` (verdict + confidence)
- **`processing/`** — input preparation: `TextPreprocessor` (hidden chars → line endings → Unicode → whitespace normalization), `MediaParser` (image/video handling)
- **`controllers/`** — REST endpoints: `DetectionController` (`POST /api/analyze/text`, `POST /api/analyze/media`), `HistoryController`, `StatusController`
- **`services/`** — `AnalyzeService` orchestrates the full flow; `RateLimiterService` (Bucket4j); `HistoryService` (persistence)
- **`entities/` + `repositories/`** — JPA entities and Spring Data repositories; Flyway migrations in `resources/db/migration/`
- **`config/`** — Spring beans, security, gRPC client config
- **`exceptions/`** — custom exceptions + `GlobalExceptionHandler`

### Analysis Flow (Text Example)

1. `POST /api/analyze/text` → `AnalyzeService.analyzeText()`
2. `TextPreprocessor` normalizes input
3. `ContentItemFactory` creates an immutable `ContentItem`
4. `AnalysisOrchestrator` runs concurrently:
   - `TextStaticAnalyzer` evaluates `TextAnalysisRule` implementations → `Evidence`
   - `TextDynamicAnalyzer` → `MLGrpcClient` → Python gRPC service (RoBERTa model)
5. `ScoreFusionStrategy.combine()` → `FinalScore`
6. Result persisted via `HistoryService`, response returned

### Adding a New Static Rule

Implement `TextAnalysisRule` (or `ImageAnalysisRule` / `VideoAnalysisRule`), register it as a Spring `@Component`, and `StaticAnalyzer` will pick it up automatically. See `ZeroWidthCharacterRule` for an example.

### gRPC Contract

Shared proto definition: `proto/ml_analyzer.proto`. Generated Java stubs live in the backend; Python stubs in `ml-service/`. When changing the proto, regenerate stubs for both services.

### Configuration

- Secrets/endpoints via `.env` (not committed)
- Spring profiles: `dev`, `prod` (`application-{profile}.yml`)
- gRPC ML service defaults to `localhost:50051`
- Multi-part uploads: 1024 MB max
- Database: H2 for tests, PostgreSQL 18 in prod/dev

### ML Models

- **Text**: RoBERTa (Hugging Face Transformers)
- **Image**: ResNet18 (PyTorch + torchvision)
- **Video**: stub (not yet implemented)
- Model weights stored locally under `ml-service/weights/` (not in repo)
