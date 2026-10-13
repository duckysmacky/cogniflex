# syntax=docker/dockerfile:1
FROM ghcr.io/astral-sh/uv:python3.12-bookworm-slim

WORKDIR /app/inference

ENV PYTHONUNBUFFERED=1
ENV UV_COMPILE_BYTECODE=1
ENV UV_LINK_MODE=copy
ENV PATH="/app/inference/.venv/bin:$PATH"

COPY inference/pyproject.toml inference/uv.lock inference/.python-version ./

RUN uv sync --locked --no-dev --no-install-project

# Download before copying sources so the layer stays cached until deps change.
# HF_TOKEN is a build secret: available to this step only, never stored in the image.
RUN --mount=type=secret,id=HF_TOKEN,env=HF_TOKEN \
    python -c "from transformers import AutoTokenizer, AutoModel; \
    AutoTokenizer.from_pretrained('roberta-base'); \
    AutoModel.from_pretrained('roberta-base')"

# Use the cached model at runtime instead of hitting Hugging Face on startup
ENV HF_HUB_OFFLINE=1

COPY inference/ .
COPY proto ../proto

RUN mkdir -p generated && \
    python -m grpc_tools.protoc \
    -I../proto \
    --python_out=./generated \
    --grpc_python_out=./generated \
    ../proto/cogniflex/inference/*.proto

CMD ["python", "main.py"]
