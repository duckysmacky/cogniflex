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
- Machine learning models (primarily via Python)

The system is designed to be:
- Modular
- Language-separated by responsibility
- Scalable from local development to production deployment

---

## Core Architectural Principle

> **Python owns models.  
Java owns logic.  
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

### 2. Backend Layer (Java)

**Primary responsibilities:**
- Input validation and normalization
- Preprocessing of text and images
- Non-ML analysis (math, logic, heuristics)
- Orchestration of analysis pipelines
- Aggregation and interpretation of results
- API exposure to frontend

**Key rules:**
- Java does **not** load or execute ML models directly
- Java does **not** contain ML frameworks
- Java treats ML as an external inference service

**Technology assumptions:**
- Java 17+
- Spring Boot
- REST (primary)
- WebSockets (optional, for streaming or async updates)

---

### 3. ML / Model Layer (Python)

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

Python is a **stateless inference service**:
- Models are loaded once at startup
- Requests are handled independently
- No persistent state unless explicitly documented

**Technology assumptions:**
- Python 3.10+
- PyTorch / TensorFlow / ONNX (model-dependent)
- FastAPI for service exposure
- NumPy for data handling

---

## Communication Between Java and Python

### Primary Mechanism: HTTP (REST)

- Java calls Python via internal REST APIs
- Payloads are structured JSON or binary (when required)
- Each ML endpoint corresponds to a **single, well-defined inference task**

**Design expectations:**
- No chatty APIs
- No implicit coupling
- Explicit request/response contracts

### Binary Data Transfer

For images or large tensors:
- Base64 is acceptable for early development
- Multipart or raw binary endpoints are preferred for production
- Metadata must always accompany binary payloads

---

## Model Handling Philosophy

### What “Model” Means Here

A model is:
- A serialized artifact (`.pt`, `.onnx`, `.pb`, etc.)
- Loaded by Python code
- Executed in memory
- Reused across requests

Models are **not**:
- Loaded per request
- Embedded in Java
- Executed via shell calls

### Model Lifecycle

1. Python service starts
2. Models are loaded into memory
3. Inference endpoints become available
4. Java sends data
5. Python returns structured results

---

## Data Flow (Conceptual)

```
Frontend
↓
Java Backend
↓
[Preprocessing / Logic / Heuristics]
↓
Python ML Service
↓
[Model Inference]
↓
Java Backend
↓
[Aggregation / Decision / Formatting]
↓
Frontend
```

At no point should the frontend or Python layer “understand” the full system logic.

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

- Java handles throughput, orchestration, and heavy logic
- Python handles numerical computation and model execution
- Premature optimization is discouraged
- Architectural correctness is prioritized over micro-optimizations

---

## Error Handling and Observability

- Python returns structured errors, never raw stack traces
- Java translates ML errors into domain-level responses
- Logging must be explicit and contextual
- Silent failure is unacceptable

---

## What AI Agents Should NEVER Do

- Introduce cross-language tight coupling
- Move business logic into Python
- Load ML models in Java
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
