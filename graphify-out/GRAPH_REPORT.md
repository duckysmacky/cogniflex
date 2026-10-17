# Graph Report - cogniflex  (2026-10-17)

## Corpus Check
- 308 files · ~71,000 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 1979 nodes · 3476 edges · 186 communities (136 shown, 50 thin omitted)
- Extraction: 96% EXTRACTED · 4% INFERRED · 0% AMBIGUOUS · INFERRED: 156 edges (avg confidence: 0.79)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `79834e86`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- InferenceGrpcClient
- InferenceAvailabilityService
- dependencies
- devDependencies
- RateLimitInterceptor
- compilerOptions
- DynamicAnalysisResult
- LiteralPhraseMatcher
- preprocess_images.py
- File schemas
- content.ts
- AnalyzeService.java
- HistoryRecord
- compilerOptions
- AnalysisOrchestrator
- ContentItem
- InferenceClient
- What You Must Do When Invoked
- TextAnalysisContextBuilder
- compilerOptions
- compilerOptions
- StaticAnalysisConfig
- .evidence
- HistoryItemResponse
- index.ts
- StaticScoringConfig
- Evidence
- .parse
- 1.B Detailed signals — Text
- new_issue.sh
- AnalysisVerdict
- LinguaTextLocaleDetector
- api.ts
- AnalysisRule
- text_preprocess_prod.py
- AnalysisOrchestratorTest.java
- 2.B Detailed signals — Image
- 3.B Detailed signals — Video
- index.ts
- Category
- SecurityConfig.java
- .preprocess
- HistoryControllerTest.java
- devDependencies
- StaticAnalyzer
- .build
- Cogniflex ML gRPC API
- index.ts
- .calculate
- Kind
- LexicalPhrasingRulesTest
- PART 4 — Cross-cutting & implementation notes
- HistoryController.java
- TextAnalysisContext
- DetectionControllerTest.java
- TextStaticAnalyzerConfigTest.java
- .from
- LeakageRulesTest.java
- HiddenCharacterRulesTest.java
- StylometryRulesTest.java
- package.json
- Attempts
- FabricatedCitationRule
- TextPreprocessor
- SemanticRulesTest.java
- StructureRulesTest.java
- Deferred endpoint-recovery solution report
- Severity
- ImageStaticAnalyzer
- StaticScoreCalculator
- EmDashDensityRule
- TypographyRulesTest.java
- chrome.ts
- ISSUE.md
- NVMe endpoint-readiness investigation
- Recovery algorithm analysis
- graphify reference: extra exports and benchmark
- ContentType
- EmojiDecorationRule
- MarkdownStructureRule
- SmartTypographyConsistencyRule
- scripts
- load_settings
- Findings
- Bus-rebalance solution report
- backend/AGENTS.md
- ConfusableScriptRule
- ConversationalScaffoldRule
- PlaceholderLeakageRule.java
- RhetoricalTemplateRule.java
- AGENTS.md
- AnalyzerServicer
- inference/AGENTS.md
- Cogniflex
- FINDINGS.md
- Handoff
- PCI bus-allocation findings
- ContentItemFactory
- TextStaticAnalyzer
- AnalysisExecutorConfig
- SwaggerConfig.java
- extension/AGENTS.md
- frontend/AGENTS.md
- RoBERTaDetector
- server.py
- Handoff
- <Issue title>
- Decisions
- PCIe/NVMe discovery progress
- Recovery failure investigation — 2026-07-24
- graphify reference: query, path, explain
- MatchTextNormalizer
- ModelInputPreparer
- 0. How to read this document
- model/AGENTS.md
- Reliable discovery of cascaded external NVMe shelves
- Firmware setup controls handoff
- HistoryRepository
- deploy.sh
- settings.store.ts
- MockVideoDetector
- Debug boot logging optimization
- graphify reference: add a URL and watch a folder
- graphify reference: commit hook and native AGENTS.md integration
- graphify reference: incremental update and cluster-only
- gradlew
- TextPreprocessingException
- React + TypeScript + Vite
- MultitypePictureDetector
- proto.py
- Inference-сервис
- Attempts
- Decisions
- Findings
- Issue catalogue
- Riser and slot observations
- graphify reference: GitHub clone and cross-repo merge
- graphify reference: transcribe video and audio
- App.tsx
- analyze.ts
- vite-env.d.ts
- tsconfig.json
- App.tsx
- vite-env.d.ts
- tsconfig.json
- Рабочая область моделей
- CONNECTIONS.md
- CONNECTIONS.md
- follow-up-prompt.md
- extraction-spec.md
- CLAUDE.md
- babel-plugin-react-compiler
- eslint
- eslint-plugin-prettier
- eslint-plugin-react-hooks
- eslint-plugin-react-refresh
- prettier
- @types/babel__core
- @types/react
- @types/react-dom
- typescript
- typescript-eslint
- @vitejs/plugin-react
- cogniflex-inference

## God Nodes (most connected - your core abstractions)
1. `StaticAnalysisConfig` - 79 edges
2. `TextAnalysisContext` - 63 edges
3. `Evidence` - 56 edges
4. `ContentItem` - 55 edges
5. `TextAnalysisRule` - 45 edges
6. `ContentType` - 35 edges
7. `DynamicAnalysisResult` - 33 edges
8. `TextAnalysisContextBuilder` - 30 edges
9. `AnalysisRule` - 28 edges
10. `AnalysisVerdict` - 26 edges

## Surprising Connections (you probably didn't know these)
- `serve()` --calls--> `load_settings()`  [INFERRED]
  inference/app/server.py → inference/app/config.py
- `_load_video_model()` --calls--> `MockVideoDetector`  [INFERRED]
  inference/app/server.py → inference/app/detectors/mocks.py
- `_load_photo_model()` --calls--> `MultitypePictureDetector`  [INFERRED]
  inference/app/server.py → inference/app/detectors/photo_detector.py
- `_load_text_model()` --calls--> `TextDetector`  [INFERRED]
  inference/app/server.py → inference/app/detectors/text_detector.py
- `serve()` --calls--> `AnalyzerServicer`  [INFERRED]
  inference/app/server.py → inference/app/grpc_service.py

## Import Cycles
- None detected.

## Communities (186 total, 50 thin omitted)

### Community 0 - "InferenceGrpcClient"
Cohesion: 0.06
Nodes (30): AfterEach, AnalyzeReply, InferenceGrpcClient, AnalyzerBlockingStub, Logger, Override, ResponseStatusException, Service (+22 more)

### Community 1 - "InferenceAvailabilityService"
Cohesion: 0.06
Nodes (34): ApplicationAvailability, GetMapping, Profile, RequestMapping, RestController, MetricsController, GetMapping, ResponseEntity (+26 more)

### Community 2 - "dependencies"
Cohesion: 0.04
Nodes (46): axios, dependencies, axios, @floating-ui/dom, @hookform/resolvers, normalize.css, react, react-dom (+38 more)

### Community 3 - "devDependencies"
Cohesion: 0.04
Nodes (47): @crxjs/vite-plugin, devDependencies, @babel/core, babel-plugin-react-compiler, @crxjs/vite-plugin, eslint, eslint-config-prettier, @eslint/js (+39 more)

### Community 4 - "RateLimitInterceptor"
Cohesion: 0.09
Nodes (24): CogniflexApplication, Configuration, EnableConfigurationProperties, Override, Profile, RateLimitConfig, ConfigurationProperties, RateLimitProperties (+16 more)

### Community 5 - "compilerOptions"
Cohesion: 0.06
Nodes (35): compilerOptions, allowImportingTsExtensions, erasableSyntaxOnly, jsx, lib, module, moduleDetection, moduleResolution (+27 more)

### Community 6 - "DynamicAnalysisResult"
Cohesion: 0.14
Nodes (12): AnalysisResult, AnalysisResultSummary, DynamicAnalysisResult, Override, FinalScore, ScoreFusionStrategy, Component, Override (+4 more)

### Community 7 - "LiteralPhraseMatcher"
Cohesion: 0.11
Nodes (14): DirectAiLeakageRule, Component, Override, Pattern, Component, Override, Pattern, KnowledgeCutoffRule (+6 more)

### Community 8 - "preprocess_images.py"
Cohesion: 0.06
Nodes (8): MultitypePictureDetector, MultitypePictureDetector_test, PictureDetector, DeepfakeDataset, Dataset, preprocess__one_video(), Dataset, VideoDataset

### Community 9 - "File schemas"
Cohesion: 0.06
Nodes (28): `ATTEMPTS.md` - append-only log, `CONNECTIONS.md` - living document, `.context/ISSUES.md` — living document, `DECISIONS.md` - append-only log, Example of self-directed issue, Example of ticket-backed issue, Examples, File kinds (+20 more)

### Community 10 - "content.ts"
Cohesion: 0.14
Nodes (28): analyzedElements, attachHoverListeners(), clearPendingTimer(), createIntersectionObserver(), createTooltip(), getMediaUrl(), getSettings(), hideTooltip() (+20 more)

### Community 11 - "AnalyzeService.java"
Cohesion: 0.15
Nodes (13): DetectionController, MultipartFile, PostMapping, RequestMapping, ResponseEntity, RestController, AnalysisResultResponse, CreateTextDetectionRequest (+5 more)

### Community 12 - "HistoryRecord"
Cohesion: 0.13
Nodes (10): InputType, MEDIA, TEXT, MediaType, IMAGE, VIDEO, HistoryRecord, ParsedMedia (+2 more)

### Community 13 - "compilerOptions"
Cohesion: 0.07
Nodes (26): compilerOptions, allowImportingTsExtensions, erasableSyntaxOnly, jsx, lib, module, moduleDetection, moduleResolution (+18 more)

### Community 14 - "AnalysisOrchestrator"
Cohesion: 0.14
Nodes (8): AnalysisOrchestrator, Logger, Service, TimedAnalysisResult, Analyzer, DynamicAnalyzer, Override, CompletionException

### Community 15 - "ContentItem"
Cohesion: 0.14
Nodes (12): ContentItem, Override, AnalysisContext, AnalysisContextBuilder, ImageAnalysisContext, ImageAnalysisContextBuilder, Component, Override (+4 more)

### Community 16 - "InferenceClient"
Cohesion: 0.12
Nodes (10): ImageDynamicAnalyzer, Component, Override, InferenceClient, Component, Override, TextDynamicAnalyzer, Component (+2 more)

### Community 17 - "What You Must Do When Invoked"
Cohesion: 0.08
Nodes (24): For /graphify add and --watch, For /graphify query, For the commit hook and native AGENTS.md integration, For --update and --cluster-only, /graphify, Honesty Rules, Interpreter guard for subcommands, Part A - Structural extraction for code files (+16 more)

### Community 18 - "TextAnalysisContextBuilder"
Cohesion: 0.18
Nodes (9): Component, TextAnalysisContextBuilder, HiddenCharacterScanner, ParagraphSplitter, SentenceSegmenter, TextLayout, WordTokenizer, Test (+1 more)

### Community 19 - "compilerOptions"
Cohesion: 0.08
Nodes (23): compilerOptions, allowImportingTsExtensions, erasableSyntaxOnly, lib, module, moduleDetection, moduleResolution, noEmit (+15 more)

### Community 20 - "compilerOptions"
Cohesion: 0.09
Nodes (22): compilerOptions, allowImportingTsExtensions, erasableSyntaxOnly, lib, module, moduleDetection, moduleResolution, noEmit (+14 more)

### Community 21 - "StaticAnalysisConfig"
Cohesion: 0.16
Nodes (11): StaticAnalysisConfig, Component, LexicalMarkerRule, Component, PerplexityRule, Component, SentenceLengthVarianceRule, Component (+3 more)

### Community 22 - ".evidence"
Cohesion: 0.13
Nodes (6): Builder, HedgingDensityRule, Component, Override, Pattern, Override

### Community 23 - "HistoryItemResponse"
Cohesion: 0.18
Nodes (11): HistoryController, GetMapping, PostMapping, RequestMapping, ResponseEntity, RestController, CreateHistoryItemRequest, HistoryItemResponse (+3 more)

### Community 24 - "index.ts"
Cohesion: 0.13
Nodes (9): AnalyzeSection(), getFileIcon(), TODO: рефактор, вынести компоненты/хуки, AnalyzeResultProps, VERDICT_TO_RESULT_CLASSES, settingsFormSchema, SettingsFormSchema, useSettingsForm() (+1 more)

### Community 25 - "StaticScoringConfig"
Cohesion: 0.15
Nodes (7): Autowired, Component, RuleConfigEntry, StaticAnalysisConfigFile, StaticRuleConfig, StaticScoringConfig, Component

### Community 26 - "Evidence"
Cohesion: 0.20
Nodes (8): Evidence, Component, Override, SteganographyRule, Component, Override, ZeroWidthCharacterRule, HiddenCharacter

### Community 27 - ".parse"
Cohesion: 0.19
Nodes (8): Component, MultipartFile, MediaParser, Component, MultipartFile, MediaTypeResolver, Test, MediaParserTest

### Community 28 - "1.B Detailed signals — Text"
Cohesion: 0.10
Nodes (21): 1.B Detailed signals — Text, T10 — AI lexical markers  *(MEDIUM)*, T11 — AI transition / cliché phrases  *(MEDIUM)*, T12 — Rhetorical-template constructions  *(LOW–MEDIUM)*, T13 — Excessive Markdown / list / bold structure  *(MEDIUM)*, T14 — Emoji-decorated headers / bullets  *(LOW–MEDIUM)*, T15 — Low sentence-length variance (burstiness)  *(MEDIUM)*, T16 — Low perplexity / high predictability  *(MEDIUM)* (+13 more)

### Community 29 - "new_issue.sh"
Cohesion: 0.13
Nodes (14): catalogue_path, context_dir, core_files, die(), issue_dir, now, require_value(), script_dir (+6 more)

### Community 30 - "AnalysisVerdict"
Cohesion: 0.16
Nodes (11): AttributeConverter, AnalysisVerdict, AI, HUMAN, fromString(), toString(), AnalysisVerdictConverter, Override (+3 more)

### Community 31 - "LinguaTextLocaleDetector"
Cohesion: 0.18
Nodes (7): DetectedTextLocale, Component, Override, LinguaTextLocaleDetector, Test, LinguaTextLocaleDetectorTest, LanguageDetector

### Community 32 - "api.ts"
Cohesion: 0.14
Nodes (9): api, apiInstance, TODO: полумать, почему не берется из енв в итоговой сборке, пока оставить фолбек, ApiProxyMethodResult, ApiProxyRequestPayload, ApiProxyKey, ApiProxyMethod, AnalyzeMediaResponse (+1 more)

### Community 33 - "AnalysisRule"
Cohesion: 0.17
Nodes (5): AnalysisRule, VideoAnalysisRule, Component, Override, VideoStaticAnalyzer

### Community 34 - "text_preprocess_prod.py"
Cohesion: 0.17
Nodes (7): predict_text_prod(), prepare_text_prod(), RoBERTaDetectorProd, AIDetectionDataset, Dataset, RoBERTaDetector, RoBERTaDetector

### Community 35 - "AnalysisOrchestratorTest.java"
Cohesion: 0.20
Nodes (5): AnalysisOrchestratorTest, EmptyTextStaticAnalyzer, Override, Test, TrackingTextDynamicAnalyzer

### Community 36 - "2.B Detailed signals — Image"
Cohesion: 0.12
Nodes (16): 2.B Detailed signals — Image, I10 — PNG/WebP delivered as a "photo"  *(LOW)*, I11 — Default/library quantization tables  *(MEDIUM)*, I12 — FFT / DCT spectral artifacts  *(MEDIUM–HIGH)*, I13 — Absent sensor-noise fingerprint (PRNU)  *(MEDIUM)*, I14 — SynthID image watermark  *(CRITICAL when detectable)*, I15 — Perceptual-hash match to known AI corpus  *(MEDIUM–HIGH)*, I1 — C2PA / Content Credentials manifest  *(CRITICAL)* (+8 more)

### Community 37 - "3.B Detailed signals — Video"
Cohesion: 0.12
Nodes (16): 3.A Summary table — Video, 3.B Detailed signals — Video, PART 3 — VIDEO, V10 — Generator-default resolution / fps  *(LOW–MEDIUM)*, V11 — Generator filename pattern  *(HIGH, renamable)*, V12 — Per-frame image artifacts sampled across frames  *(MEDIUM–HIGH)*, V13 — Missing motion/GPS metadata  *(LOW)*, V1 — C2PA in the video container  *(CRITICAL)* (+8 more)

### Community 38 - "index.ts"
Cohesion: 0.12
Nodes (5): ButtonProps, DividerProps, InputProps, SwitchProps, TextAreaProps

### Community 39 - "Category"
Cohesion: 0.13
Nodes (13): Category, ENCODING, FILENAME, HIDDEN_CHARACTERS, LEAKAGE, LEXICAL, METADATA, PHRASING (+5 more)

### Community 40 - "SecurityConfig.java"
Cohesion: 0.23
Nodes (10): CorsProperties, ConfigurationProperties, Bean, Configuration, EnableConfigurationProperties, SecurityConfig, CorsConfigurationSource, EnableWebSecurity (+2 more)

### Community 41 - ".preprocess"
Cohesion: 0.25
Nodes (4): TextPreprocessingOptions, Test, TextPreprocessorTest, Form

### Community 42 - "HistoryControllerTest.java"
Cohesion: 0.28
Nodes (9): HistoryControllerTest, ActiveProfiles, BeforeEach, ExtendWith, MockMvc, ObjectMapper, Test, WebMvcTest (+1 more)

### Community 43 - "devDependencies"
Cohesion: 0.13
Nodes (15): devDependencies, @babel/core, eslint-config-prettier, @eslint/js, globals, @rolldown/plugin-babel, @types/node, vite (+7 more)

### Community 44 - "StaticAnalyzer"
Cohesion: 0.23
Nodes (7): Logger, Override, StaticAnalyzer, CogniflexBackendApplicationTests, ActiveProfiles, Test, SpringBootTest

### Community 45 - ".build"
Cohesion: 0.31
Nodes (3): Override, Test, TextAnalysisContextBuilderTest

### Community 46 - "Cogniflex ML gRPC API"
Cohesion: 0.14
Nodes (14): AnalyzePhoto, AnalyzeText, AnalyzeVideo, Cogniflex ML gRPC API, Proto, Где находится контракт, Запуск сервера, Конфигурация (+6 more)

### Community 47 - "index.ts"
Cohesion: 0.23
Nodes (11): Dictionary, en, DICTIONARIES, getT(), Language, LANGUAGE_NAMES, LanguageSetting, resolveLanguage() (+3 more)

### Community 49 - "Kind"
Cohesion: 0.18
Nodes (11): fromCodePoint(), isUnsafeControl(), Kind, BIDI_CONTROL, BYTE_ORDER_MARK, CONTROL, NON_BREAKING_SPACE, SOFT_HYPHEN (+3 more)

### Community 51 - "PART 4 — Cross-cutting & implementation notes"
Cohesion: 0.15
Nodes (12): 1.A Summary table — Text, 2.A Summary table — Image, 4.1 Signals that span all modalities, 4.2 Suggested rule-engine wiring (maps to your Java design), 4.3 Suggested Java libraries / tooling, 4.4 Severity → weight mapping cheat-sheet, 4.5 Composing static + dynamic into the final verdict, 4.6 Anti-evasion reminders (+4 more)

### Community 52 - "HistoryController.java"
Cohesion: 0.30
Nodes (3): MediaParsingException, HttpStatus, ResponseStatusException

### Community 53 - "TextAnalysisContext"
Cohesion: 0.21
Nodes (7): Override, Component, Override, Pattern, RepetitiveOpenerRule, Override, TextAnalysisContext

### Community 54 - "DetectionControllerTest.java"
Cohesion: 0.30
Nodes (8): DetectionControllerTest, ActiveProfiles, ExtendWith, MockMvc, ObjectMapper, Test, WebMvcTest, WithMockUser

### Community 55 - "TextStaticAnalyzerConfigTest.java"
Cohesion: 0.23
Nodes (5): Component, Component, Component, Test, TextStaticAnalyzerConfigTest

### Community 56 - ".from"
Cohesion: 0.17
Nodes (5): LineEndingNormalization, ModelInput, NormalizationStats, PreprocessedText, WhitespaceNormalization

### Community 57 - "LeakageRulesTest.java"
Cohesion: 0.35
Nodes (3): Component, Test, LeakageRulesTest

### Community 60 - "package.json"
Cohesion: 0.17
Nodes (11): dependencies, normalize.css, react, react-dom, normalize.css, react, react-dom, name (+3 more)

### Community 61 - "Attempts"
Cohesion: 0.18
Nodes (10): 2026-05-21 - Increase root-port extra-bus reservation, 2026-06-08 - Force a 2:6 bus ratio between sockets, 2026-06-08 - Override the ratio in source corresponding to a packaged binary, 2026-06-26 - Override the reinstalled policy from a notification callback, 2026-07-17 - Enable two PCI segments and rebalance stack windows, 2026-07-20 - Add bounded identity and BAR retries during enumeration, 2026-07-21 - Retry controller-register reads in the NVMe driver, 2026-07-24 - Gate on link state and issue one leaf-port secondary-bus reset (+2 more)

### Community 62 - "FabricatedCitationRule"
Cohesion: 0.31
Nodes (4): FabricatedCitationRule, Component, Override, Pattern

### Community 63 - "TextPreprocessor"
Cohesion: 0.24
Nodes (4): TextLocaleDetector, Component, TextPreprocessor, FunctionalInterface

### Community 66 - "Deferred endpoint-recovery solution report"
Cohesion: 0.20
Nodes (10): Correctness constraints, Deferred endpoint-recovery solution report, Deferred sequence, Identity absent, Invalid BAR, Result, Summary, Two failure modes (+2 more)

### Community 67 - "Severity"
Cohesion: 0.22
Nodes (6): atLeast(), Severity, CRITICAL, HIGH, LOW, MEDIUM

### Community 68 - "ImageStaticAnalyzer"
Cohesion: 0.24
Nodes (4): ImageAnalysisRule, ImageStaticAnalyzer, Component, Override

### Community 70 - "EmDashDensityRule"
Cohesion: 0.33
Nodes (4): EmDashDensityRule, Component, Override, Pattern

### Community 72 - "chrome.ts"
Cohesion: 0.20
Nodes (7): ChromeRuntimeCallback, ChromeRuntimeMessage, ChromeRuntimeMessageResponse, ChromeRuntimeMessageType, AbstractFunction, AbstractObject, Nullable

### Community 73 - "ISSUE.md"
Cohesion: 0.22
Nodes (5): Boot matrix, FW-1427 verification procedure, Linux, UEFI Shell, Starting prompt

### Community 74 - "NVMe endpoint-readiness investigation"
Cohesion: 0.22
Nodes (9): BAR classification, Common pattern, Failure signature A — identity absent, Failure signature B — identity valid, BAR invalid, Identity classification, Measurement, NVMe endpoint-readiness investigation, Observation boundary (+1 more)

### Community 75 - "Recovery algorithm analysis"
Cohesion: 0.22
Nodes (8): Implementation boundaries, Placement, Policy budgets, Quarantine, Recovery algorithm analysis, Reset boundaries, Scope gate, State machine

### Community 76 - "graphify reference: extra exports and benchmark"
Cohesion: 0.22
Nodes (8): graphify reference: extra exports and benchmark, Step 6b - Wiki (only if --wiki flag), Step 7 - Neo4j export (only if --neo4j or --neo4j-push flag), Step 7a - FalkorDB export (only if --falkordb or --falkordb-push flag), Step 7b - SVG export (only if --svg flag), Step 7c - GraphML export (only if --graphml flag), Step 7d - MCP server (only if --mcp flag), Step 8 - Token reduction benchmark (only if total_words > 5000)

### Community 77 - "ContentType"
Cohesion: 0.31
Nodes (5): ContentType, IMAGE, TEXT, VIDEO, from()

### Community 78 - "EmojiDecorationRule"
Cohesion: 0.33
Nodes (4): EmojiDecorationRule, Component, Override, Pattern

### Community 79 - "MarkdownStructureRule"
Cohesion: 0.36
Nodes (4): Component, Override, Pattern, MarkdownStructureRule

### Community 80 - "SmartTypographyConsistencyRule"
Cohesion: 0.36
Nodes (4): Component, Override, Pattern, SmartTypographyConsistencyRule

### Community 81 - "scripts"
Cohesion: 0.22
Nodes (9): scripts, build, dev, format, format:check, lint, lint:fix, preview (+1 more)

### Community 82 - "load_settings"
Cohesion: 0.44
Nodes (8): GrpcSettings, ImageModelSettings, load_env_file(), load_settings(), Path, _resolve_service_path(), Settings, TextModelSettings

### Community 83 - "Findings"
Cohesion: 0.25
Nodes (8): 2026-05-21 - Root-port padding cannot enlarge the stack window, 2026-06-08 - Parallel and cascaded shelves consume bus windows differently, 2026-06-26 - A precompiled initialization component overwrites the supplied socket ratio, 2026-07-17 - The bus-number shortage is resolved, 2026-07-20 - Remaining losses have two endpoint-readiness signatures, 2026-07-24 - Aggressive passive retry and the first reset escalation were insufficient, 2026-07-31 - Deferred recovery produced the first 11-boot clean run, Findings

### Community 84 - "Bus-rebalance solution report"
Cohesion: 0.25
Nodes (8): Bus-rebalance solution report, Parallel versus cascade, Problem statement, Rebalance algorithm, Result and remaining issue, Safety and controls, Two allocation levels, Why the reported numbers were misleading

### Community 85 - "backend/AGENTS.md"
Cohesion: 0.25
Nodes (7): backend/AGENTS.md, Build and run, Common tasks, Java style, Package layout, Stack, Testing

### Community 86 - "ConfusableScriptRule"
Cohesion: 0.36
Nodes (3): ConfusableScriptRule, Component, Override

### Community 87 - "ConversationalScaffoldRule"
Cohesion: 0.39
Nodes (4): ConversationalScaffoldRule, Component, Override, Pattern

### Community 88 - "PlaceholderLeakageRule.java"
Cohesion: 0.36
Nodes (5): Component, Override, Pattern, PlaceholderLeakageRule, PlaceholderPattern

### Community 89 - "RhetoricalTemplateRule.java"
Cohesion: 0.36
Nodes (5): Component, Override, Pattern, RhetoricalTemplateRule, Template

### Community 90 - "AGENTS.md"
Cohesion: 0.25
Nodes (7): AGENTS.md, Architecture, Git workflow, Guardrails, Repository map, Tools and skills, What Cogniflex is

### Community 91 - "AnalyzerServicer"
Cohesion: 0.46
Nodes (3): AnalyzerServicer, _reply_from_prediction(), _set_internal_error()

### Community 92 - "inference/AGENTS.md"
Cohesion: 0.25
Nodes (7): Build and run, Checks before pushing, inference/AGENTS.md, Layout, Model weights, Rules, Stack

### Community 93 - "Cogniflex"
Cohesion: 0.25
Nodes (7): About, Cogniflex, Development, Features, Manual analysis tools, Multi-method detection, Real-time content analysis

### Community 95 - "Handoff"
Cohesion: 0.29
Nodes (5): Current state, Handoff, Last thing tried, Next step, Warning

### Community 96 - "PCI bus-allocation findings"
Cohesion: 0.29
Nodes (6): Allocation model, Build-consumer discovery, Evidence that allocation is fixed, Final allocation direction, PCI bus-allocation findings, Why root-port padding failed

### Community 98 - "TextStaticAnalyzer"
Cohesion: 0.38
Nodes (3): Component, Override, TextStaticAnalyzer

### Community 99 - "AnalysisExecutorConfig"
Cohesion: 0.48
Nodes (3): AnalysisExecutorConfig, Bean, Configuration

### Community 100 - "SwaggerConfig.java"
Cohesion: 0.48
Nodes (5): Bean, Configuration, Profile, SwaggerConfig, OpenAPI

### Community 102 - "extension/AGENTS.md"
Cohesion: 0.29
Nodes (6): Build and run, Checks before pushing, extension/AGENTS.md, Layout, Rules, Stack

### Community 103 - "frontend/AGENTS.md"
Cohesion: 0.29
Nodes (6): Build and run, Checks before pushing, frontend/AGENTS.md, Layout, Rules, Stack

### Community 105 - "server.py"
Cohesion: 0.48
Nodes (6): _load_photo_model(), _load_text_model(), _load_video_model(), _preload_models(), Path, serve()

### Community 106 - "Handoff"
Cohesion: 0.33
Nodes (5): Current state, Handoff, Last thing tried, Next step, Warning

### Community 107 - "<Issue title>"
Cohesion: 0.33
Nodes (5): Acceptance criteria, <Issue title>, Links, Scope, Summary

### Community 108 - "Decisions"
Cohesion: 0.33
Nodes (5): 2026-05-21 - Investigate platform bus allocation, 2026-06-08 - Limit scope to cascaded single-slot topology, 2026-06-08 - Reclaim capacity from lightly used I/O stacks, 2026-07-20 - Test shelf-side delay independently, Decisions

### Community 109 - "PCIe/NVMe discovery progress"
Cohesion: 0.33
Nodes (5): Current result, PCIe/NVMe discovery progress, Phase 1 — resource diagnosis, Phase 2 — intermittent endpoint failures, Recovery evolution

### Community 110 - "Recovery failure investigation — 2026-07-24"
Cohesion: 0.33
Nodes (6): Follow-up, Interpretation, Recovery failure investigation — 2026-07-24, Required changes, Results, Test context

### Community 111 - "graphify reference: query, path, explain"
Cohesion: 0.33
Nodes (5): For /graphify explain, For /graphify path, graphify reference: query, path, explain, Step 0 — Constrained query expansion (REQUIRED before traversal), Step 1 — Traversal

### Community 112 - "MatchTextNormalizer"
Cohesion: 0.53
Nodes (3): Component, MatchTextNormalizer, Normalizer2

### Community 114 - "0. How to read this document"
Cohesion: 0.33
Nodes (6): 0.1 Terminology (matches the system design), 0.2 The scoring contract (suggestion), 0.3 Two weight philosophies — pick deliberately per rule, 0.4 Combination bonus (strongly recommended), 0.5 False-positive discipline (calibration notes), 0. How to read this document

### Community 116 - "model/AGENTS.md"
Cohesion: 0.33
Nodes (5): Domain notes, Layout, model/AGENTS.md, Rules, Stack

### Community 117 - "Reliable discovery of cascaded external NVMe shelves"
Cohesion: 0.40
Nodes (5): Acceptance criteria, Links, Reliable discovery of cascaded external NVMe shelves, Scope, Summary

### Community 118 - "Firmware setup controls handoff"
Cohesion: 0.40
Nodes (4): Control 1 — cascade bus allocation, Control 2 — cascade slot, Firmware setup controls handoff, Validation

### Community 119 - "HistoryRepository"
Cohesion: 0.60
Nodes (3): HistoryRepository, JpaRepository, Repository

### Community 120 - "deploy.sh"
Cohesion: 0.70
Nodes (4): log(), remove_old_project_images(), remove_project_images(), deploy.sh script

### Community 123 - "Debug boot logging optimization"
Cohesion: 0.50
Nodes (3): Debug boot logging optimization, Keep, Remove

### Community 124 - "graphify reference: add a URL and watch a folder"
Cohesion: 0.50
Nodes (3): For /graphify add, For --watch, graphify reference: add a URL and watch a folder

### Community 125 - "graphify reference: commit hook and native AGENTS.md integration"
Cohesion: 0.50
Nodes (3): For git commit hook, For native AGENTS.md integration, graphify reference: commit hook and native AGENTS.md integration

### Community 126 - "graphify reference: incremental update and cluster-only"
Cohesion: 0.50
Nodes (3): For --cluster-only, For --update (incremental re-extraction), graphify reference: incremental update and cluster-only

### Community 127 - "gradlew"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

### Community 129 - "React + TypeScript + Vite"
Cohesion: 0.50
Nodes (3): Expanding the ESLint configuration, React Compiler, React + TypeScript + Vite

### Community 132 - "Inference-сервис"
Cohesion: 0.50
Nodes (3): Inference-сервис, Запуск, Структура

## Knowledge Gaps
- **530 isolated node(s):** `VALID_KINDS`, `script_dir`, `templates_dir`, `context_dir`, `issue_dir` (+525 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **50 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `ContentItem` connect `ContentItem` to `AnalyzeService.java`, `AnalysisOrchestrator`, `InferenceClient`, `TextAnalysisContextBuilder`, `AnalysisOrchestratorTest.java`, `StaticAnalyzer`, `.build`, `LexicalPhrasingRulesTest`, `TextAnalysisContext`, `TextStaticAnalyzerConfigTest.java`, `LeakageRulesTest.java`, `HiddenCharacterRulesTest.java`, `StylometryRulesTest.java`, `SemanticRulesTest.java`, `StructureRulesTest.java`, `StaticScoreCalculator`, `TypographyRulesTest.java`, `ContentType`, `ContentItemFactory`?**
  _High betweenness centrality (0.038) - this node is a cross-community bridge._
- **Why does `TextAnalysisContext` connect `TextAnalysisContext` to `LiteralPhraseMatcher`, `ContentItem`, `TextAnalysisContextBuilder`, `StaticAnalysisConfig`, `.evidence`, `Evidence`, `.build`, `LexicalPhrasingRulesTest`, `LeakageRulesTest.java`, `HiddenCharacterRulesTest.java`, `StylometryRulesTest.java`, `FabricatedCitationRule`, `SemanticRulesTest.java`, `StructureRulesTest.java`, `EmDashDensityRule`, `TypographyRulesTest.java`, `EmojiDecorationRule`, `MarkdownStructureRule`, `SmartTypographyConsistencyRule`, `ConfusableScriptRule`, `ConversationalScaffoldRule`, `PlaceholderLeakageRule.java`, `RhetoricalTemplateRule.java`, `TextStaticAnalyzer`?**
  _High betweenness centrality (0.038) - this node is a cross-community bridge._
- **Why does `Evidence` connect `Evidence` to `RhetoricalTemplateRule.java`, `AnalysisRule`, `Severity`, `StaticScoreCalculator`, `DynamicAnalysisResult`, `LiteralPhraseMatcher`, `EmDashDensityRule`, `AnalyzeService.java`, `EmojiDecorationRule`, `MarkdownStructureRule`, `SmartTypographyConsistencyRule`, `StaticAnalysisConfig`, `.evidence`, `ConfusableScriptRule`, `ConversationalScaffoldRule`, `TextAnalysisContext`, `PlaceholderLeakageRule.java`, `FabricatedCitationRule`?**
  _High betweenness centrality (0.035) - this node is a cross-community bridge._
- **Are the 7 inferred relationships involving `ContentItem` (e.g. with `.context()` and `.context()`) actually correct?**
  _`ContentItem` has 7 INFERRED edges - model-reasoned connections that need verification._
- **What connects `VALID_KINDS`, `script_dir`, `templates_dir` to the rest of the system?**
  _530 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `InferenceGrpcClient` be split into smaller, more focused modules?**
  _Cohesion score 0.06201923076923077 - nodes in this community are weakly interconnected._
- **Should `InferenceAvailabilityService` be split into smaller, more focused modules?**
  _Cohesion score 0.05961538461538462 - nodes in this community are weakly interconnected._