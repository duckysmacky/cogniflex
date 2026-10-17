# inference/AGENTS.md

Python gRPC service that serves the ML models to the backend. Read the root `AGENTS.md` first.

## Stack

- Python **3.12** (`.python-version`), managed by **uv** (`pyproject.toml` + `uv.lock`, `package = false`)
- **just** as the task runner (`justfile`)
- `grpcio` / `grpcio-tools`, `protobuf`; stubs generated from `../proto` into `generated/` (gitignored)
- PyTorch + torchvision from the **CPU-only** index (`pytorch-cpu` in `pyproject.toml`), Hugging Face
  `transformers` (RoBERTa text model), ResNet18 image models, OpenCV headless, Pillow, safetensors, PyYAML
- Docker image: `ghcr.io/astral-sh/uv:python3.12-bookworm-slim` (see `Dockerfile`); `roberta-base`
  is downloaded at build time with `HF_TOKEN` passed as a build secret

## Build and run

```bash
cd inference
just sync                       # uv sync -> .venv
just proto                      # generate gRPC stubs (main.py also does this if stale)
just run                        # gRPC server on $GRPC_HOST:$GRPC_PORT (default [::]:50051)
just test-text "some text"      # manual client against a running server
just test-photo path/to.jpg
just test-video path/to.mp4
just                            # list all recipes
```

- Weights listed in `config.yaml` must exist in `weights/` or the server will not start.
  Download sources belong in `inference/README.md`; if a source is missing there, ask the user.
- Env vars come from the root `.env` (`GRPC_HOST`, `GRPC_PORT`, `HF_TOKEN`).
- Never use `pip install` or a hand-made venv. Add deps with `uv add <pkg>` (`uv add --dev` for
  dev-only tools) and commit `uv.lock`. Keep torch/torchvision on the CPU index; never pull CUDA wheels.

## Layout

- `main.py` - entry point: path setup, proto generation, then `app.server.serve`
- `app/config.py` - frozen dataclass settings from `config.yaml` + env
- `app/proto.py` - stub generation when `generated/` is missing or stale
- `app/server.py` - gRPC server setup and model preloading
- `app/grpc_service.py` - `AnalyzerServicer`, one method per RPC in `analyzer.proto`
- `app/detectors/` - model wrappers (`*_detector.py`), stubs in `mocks.py` (video is a stub)
- `app/inference/` - runtime preprocessing helpers
- `tests/` - manual gRPC client scripts (`test_*_client.py`), they need a running server
- `weights/` - local model files (gitignored except `.gitkeep`)

## Rules

- **Type hints on every function and method**, parameters and return type (`-> None` too).
  Use modern syntax: `list[str]`, `dict[str, float]`, `X | None`, `collections.abc` types.
  Add hints to existing functions you touch.
- Detectors expose a small typed API (e.g. `predict_text(text: str) -> tuple[float, str]`) and hide
  framework details. The servicer only validates input, calls a detector and maps the result to
  `AnalyzeReply`.
- Load models once at startup, never per request. Run inference under `torch.inference_mode()` / `torch.no_grad()`
  and keep the shared inference lock when touching non-thread-safe models.
- gRPC errors: set `context.set_code(...)` + `set_details(...)` (`INVALID_ARGUMENT` for bad input,
  `INTERNAL` for failures) and return an empty `AnalyzeReply`; never let exceptions crash the server.
- Logging via `logging`, never `print`. Do not log request payloads (user text, images, videos).
- Paths via `pathlib.Path`, resolved relative to the service root; configurable values go to
  `config.yaml` or env, not constants scattered in code.
- **Never import from `model/`.** If research code is needed in production, port a clean,
  typed version into `app/`.
- Names: `snake_case` modules and functions, `PascalCase` classes, one detector per module.
- Proto changes: edit `../proto/cogniflex/inference/analyzer.proto`, run `just proto`, update the
  servicer and make sure the backend still builds.

## Model weights

- Never commit weights (`*.pth`, `*.safetensors`, `*.onnx`, ...) or downloaded HF caches.
- Adding or replacing a model: update `config.yaml` paths and add the file's download source,
  expected filename and target path (`inference/weights/<file>`) to `inference/README.md`.
- If you do not know where a weights file came from, ask the user instead of guessing.

## Checks before pushing

- `just sync` succeeds and `just run` starts with local weights.
- Exercise changed RPCs with the `just test-*` clients.
- If you add automated tests, use `pytest` (`uv add --dev pytest`) under `tests/`, keep them
  free of real weights by using the mocks.
