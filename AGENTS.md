# ROBOTS.md  
Guidelines for AI Coding Agents and Automated Contributors

## Purpose of This File

This document exists to align **AI coding agents** with the **architectural intent, constraints, and philosophy** of this project.

If you are an automated system generating, modifying, or reviewing code in this repository, you are expected to:
- Respect the system boundaries described here
- Preserve architectural separation
- Prefer clarity, explicitness, and long-term maintainability over cleverness
- Assume this project is developed by a multi-person team with mixed skill levels

This is not a tutorial. This is a contract.

---

## Project Overview (High-Level)

This project is a **multi-modal analysis platform** that processes:
- **Text data**
- **Image data**

The analysis is performed using a **hybrid approach**:
- Deterministic algorithms (math, heuristics, rules, classical detection)
- Machine learning models (served by the Model layer)

The system is designed to be:
- Modular
- Domain-separated by responsibility
- Scalable from local development to production deployment

---

## Repository Structure

The repository is organized around four primary product domains:

- `frontend/` - The website frontend. This contains the web application that communicates with the Backend over public-facing APIs. Keep website-specific UI, routing, state management, assets, and frontend build configuration here.
- `extension/` - The browser extension frontend. It is used for extension-specific UI, scripts, manifests, packaging, and integration logic. Do not mix extension code into `frontend/`.
- `backend/` - The Backend service. This is the orchestration and API layer responsible for validation, preprocessing, heuristics, aggregation, and communication with the Model service.
- `model/` - The Model service. This is the inference domain and contains model-serving code, model-specific preprocessing/postprocessing, and research artifacts that support model development.

Agents should preserve these boundaries. If functionality belongs to a specific product surface or runtime, place it in that domain directory or create a new one rather than creating cross-domain shortcuts.

### Directory Expectations

- `frontend/` is the website application root. Treat it as independent from the extension, even if both eventually share similar user-facing behavior.
- `extension/` keep browser-extension concerns isolated there instead of reusing website entrypoints directly.
- `backend/` should contain application source, resources, tests, build configuration, API contracts owned by the Backend, and integration code for talking to the Model service.
- `model/` should contain only model-serving and model-development concerns.

### Model Directory Guidance

The `model/` directory should be used as follows:

- `model/app/` - Production Model service code. Put gRPC handlers, model loading, inference orchestration, model-specific preprocessing/postprocessing, configuration wiring, and shared internal Python modules here.
- `model/notebooks/` - Research and exploration only. Use notebooks for experiments, evaluation, prototyping, and investigation. Do not import notebooks into production code.
- `model/requirements.txt` - Python dependencies for the Model service and related development workflows.
- `model/build/` - Generated or transient build artifacts. Agents should avoid relying on committed generated outputs unless the repository intentionally tracks them.

If additional structure is introduced under `model/`, keep it explicit. For example, separate production service code, reusable model utilities, trained artifacts metadata, and experiments rather than mixing them in one folder.

### Placement Rules

- Shared product behavior across website and extension does not justify merging them into one directory. Prefer duplication over accidental coupling until a deliberate shared package exists.
- Backend-to-Model contracts must remain explicit and versioned. If protobuf definitions are introduced, store them in a clearly owned location and do not scatter copies across domains.
- Generated assets, build outputs, caches, and notebooks should not become substitutes for maintainable source modules.
- When in doubt, place orchestration and business decisions in `backend/`, model execution in `model/`, website concerns in `frontend/`, and extension concerns in `extension/`.

---

## Core Architectural Principle

> **Model owns models.  
Backend owns logic.  
APIs connect them.**

No layer should leak its responsibilities into another.

---

## System Layers

### 1. Frontend Layer (Out of Scope for Agents Unless Explicitly Stated)

Includes:
- Web UI
- Browser extension (UI + logic)

Communicates with backend via HTTP/WebSocket APIs.

AI agents should **not** modify frontend code unless explicitly instructed.

---

### 2. Backend Layer

**Primary responsibilities:**
- Input validation and normalization
- Preprocessing of text and images
- Non-ML analysis (math, logic, heuristics)
- Orchestration of analysis pipelines
- Aggregation and interpretation of results
- API exposure to frontend

**Key rules:**
- Backend does **not** load or execute ML models directly
- Backend does **not** contain ML frameworks
- Backend treats the Model as an external inference service

**Technology assumptions:**
- Java 25+
- Spring Boot
- REST (primary)
- WebSockets (optional, for streaming or async updates)

---

### 3. ML / Model Layer

**Primary responsibilities:**
- Model loading
- Model inference
- Minimal, model-specific preprocessing/postprocessing
- Returning structured inference results

**Explicit non-responsibilities:**
- Business logic
- Feature orchestration
- Workflow decisions
- Data validation beyond model requirements

The Model layer is a **stateless inference service**:
- Models are loaded once at startup
- Requests are handled independently
- No persistent state unless explicitly documented

**Technology assumptions:**
- Python 3.10+
- PyTorch / TensorFlow / ONNX (model-dependent)
- gRPC for service exposure
- NumPy for data handling

---

## Communication Between Backend and Model

### Primary Mechanism: gRPC

- Backend calls the Model service via internal gRPC services
- Payloads are structured via explicit protobuf contracts
- Each RPC corresponds to a **single, well-defined inference task**

**Design expectations:**
- No chatty APIs
- No implicit coupling
- Explicit request/response contracts defined in `.proto` files

### Binary Data Transfer

For images or large tensors:
- Prefer raw bytes in protobuf messages where practical
- Use streaming RPCs only when payload size or workflow requirements justify them
- Metadata must always accompany binary payloads

---

## Model Handling Philosophy

### What “Model” Means Here

A model is:
- A serialized artifact (`.pt`, `.onnx`, `.pb`, etc.)
- Loaded by Model service code
- Executed in memory
- Reused across requests

Models are **not**:
- Loaded per request
- Embedded in the Backend
- Executed via shell calls

### Model Lifecycle

1. Model service starts
2. Models are loaded into memory
3. Inference RPCs become available
4. Backend sends data
5. Model returns structured results

---

## Data Flow (Conceptual)

```
Frontend
↓
Backend
↓
[Preprocessing / Logic / Heuristics]
↓
Model Service
↓
[Model Inference]
↓
Backend
↓
[Aggregation / Decision / Formatting]
↓
Frontend
```

At no point should the frontend or Model layer “understand” the full system logic.

AI agents must **not** flatten or merge these layers.

---

## Jupyter Notebooks Policy

- `.ipynb` files are allowed **only** in `model/notebooks/`
- Notebooks are for research, prototyping, and exploration
- Production logic must be rewritten into `.py` modules
- Notebooks should never be imported by production code

---

## Git and Collaboration Expectations

AI agents must:
- Generate atomic, reviewable changes
- Avoid sweeping refactors unless explicitly requested
- Preserve formatting and existing conventions
- Never silently delete code without justification

Branch naming conventions (examples):
- `feature/<short-description>`
- `fix/<issue>`

---

## Performance Philosophy

- Backend handles throughput, orchestration, and heavy logic
- Model handles numerical computation and model execution
- Premature optimization is discouraged
- Architectural correctness is prioritized over micro-optimizations

---

## Error Handling and Observability

- Model returns structured errors, never raw stack traces
- Backend translates model errors into domain-level responses
- Logging must be explicit and contextual
- Silent failure is unacceptable

---

## What AI Agents Should NEVER Do

- Introduce tight coupling between Backend and Model
- Move business logic into the Model layer
- Load ML models in the Backend
- Use reflection or runtime hacks to “simplify” design
- Bypass documented APIs
- Assume single-developer ownership

---

## Final Instruction

If you are uncertain:
- Preserve existing structure
- Ask for clarification (via comments or TODOs)
- Prefer explicit code over inferred behavior

This system is designed to grow.
Your job is to help it do so without collapsing under its own cleverness.
