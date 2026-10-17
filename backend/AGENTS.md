# backend/AGENTS.md

Spring Boot REST API and analysis pipeline. Read the root `AGENTS.md` first for workflow and guardrails.

## Stack

- Java **25** (Gradle toolchain), Spring Boot **4.0.x**, Gradle **9.x** via the wrapper (`build.gradle`, Groovy DSL)
- Spring Web MVC, Data JPA, Security, Validation, springdoc-openapi (Swagger UI)
- PostgreSQL 18 + **Flyway** (`src/main/resources/db/migration/`), Hibernate `ddl-auto: validate`
- Bucket4j rate limiting (`InMemoryRateLimitService`); Redis is used for status/metrics availability checks
- gRPC client: stubs generated from `../proto` by the `com.google.protobuf` Gradle plugin
  into package `io.github.duckysmacky.cogniflex.grpc` (build output, never committed)
- Text processing: ICU4J, Lingua (language detection), Aho-Corasick (phrase matching)
- Tests: JUnit 5, Spring Boot test slices, H2 in-memory DB, in-process gRPC

## Build and run

```bash
cd backend
./gradlew build          # compile, generate gRPC stubs, run tests
./gradlew test           # tests only (CI runs exactly this)
./gradlew test --tests '*TextStaticAnalyzer*'   # single test class
./gradlew bootRun --args='--spring.profiles.active=dev'   # needs Postgres, Redis, inference
```

- Config: `application.yml` + `application-{dev,prod}.yml`; values from env / root `.env`.
- Local deps without the full stack: `docker compose up -d db redis inference` from the repo root.
- Always use `./gradlew`, never a system `gradle`. Do not bump the wrapper or plugin versions unasked.

## Package layout

Everything lives under `io.github.duckysmacky.cogniflex`:

- `analysis/` - `Analyzer`, `AnalysisOrchestrator` (static + dynamic in parallel), `ContentItem`,
  `ContentItemFactory`, `AnalysisVerdict`
  - `static_/` - `StaticAnalyzer`, `AnalysisRule`, `Evidence`, `StaticScoreCalculator`; `text/`,
    `image/`, `video/` with their rules (`text/rules/`) and `config/` (bound from
    `resources/analysis/static-analysis-config.yml`)
  - `dynamic/` - `DynamicAnalyzer`s; `dynamic/inference/` holds the `InferenceClient` interface
    and its gRPC implementation `InferenceGrpcClient`
  - `score/` - `ScoreFusionStrategy` -> `FinalScore`
- `processing/` - `text/` (normalization, segmentation, locale detection, model input) and `media/` (parsing)
- `controllers/` - `/api/analyze/{text,media}`, `/api/history`, `/api/status`, `/api/metrics`
- `services/` - `AnalyzeService`, `HistoryService`, `StatusService`, rate limiting,
  `availability/` (DB, Redis, inference health)
- `dto/`, `entities/`, `repositories/`, `converters/`, `config/`, `exceptions/` (`GlobalExceptionHandler`)

Request flow: controller -> `AnalyzeService` -> preprocess -> `AnalysisOrchestrator`
(static rules + `InferenceGrpcClient`) -> score fusion -> `HistoryService` -> response DTO.

## Common tasks

- **New static text rule**: implement `TextAnalysisRule` in `analysis/static_/text/rules/`,
  register it as a Spring `@Component`, add its weight/thresholds to `static-analysis-config.yml`,
  put word lists in `resources/analysis/text/<category>/*-<lang>.txt`, and add tests next to the
  existing rule tests. `ZeroWidthCharacterRule` is a minimal example.
- **New endpoint**: thin controller under `/api/...`, logic in a service, request/response as
  `record` DTOs with Bean Validation, errors through `GlobalExceptionHandler`.
- **Schema change**: add a new `V<n>__<snake_case_description>.sql`. Never edit an existing migration.
- **Proto change**: edit `proto/`, rebuild here and regenerate inference stubs (`just proto`).

## Java style

Use modern Java 25 idioms (refactor old code toward them only when you are already changing it):
- `record`s for DTOs, value objects, results and `@ConfigurationProperties`.
- `sealed` interfaces + pattern matching (`switch` with patterns, `instanceof` binding) over
  type checks and casts; switch **expressions**, not fall-through statements.
- `var` only when the type is obvious from the right-hand side. Text blocks for multi-line strings.
- Immutable collections: `List.of`, `Map.of`, `Stream.toList()`; return empty collections, not `null`.
- `Optional` only as a return type, never as a field or parameter.
- Constructor injection with `final` fields; no field `@Autowired`. No Lombok.
- Explicit imports, no wildcards in new code. Descriptive names; no abbreviations.
- Small focused classes; package by layer as above. Keep controllers free of business logic.
- Log through SLF4J with parameters (`log.info("x={}", x)`), never string concatenation.

## Testing

- Every new rule, service method or endpoint gets tests. Mirror the main package path in `src/test`.
- Tests should be written to test the expected behaviour, not just to verify the code. They can and should fail if the
  implementation fails to deliver that expected result. Do not just write tests for the sake of having 100% code green
- Controller tests use `@WebMvcTest` slices with `@MockitoBean` (`@MockBean` is gone in Boot 4);
  mock `InferenceClient` rather than needing a live inference server. Tests must pass without network, Postgres or Redis.
- `./gradlew test` must be green before pushing.
