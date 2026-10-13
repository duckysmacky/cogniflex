FROM ghcr.io/astral-sh/uv:python3.12-bookworm-slim

WORKDIR /app/inference

ARG HF_TOKEN

ENV PYTHONUNBUFFERED=1
ENV HF_TOKEN=${HF_TOKEN}
ENV UV_COMPILE_BYTECODE=1
ENV UV_LINK_MODE=copy
ENV PATH="/app/inference/.venv/bin:$PATH"

COPY inference/pyproject.toml inference/uv.lock inference/.python-version ./

RUN uv sync --locked --no-dev --no-install-project

COPY inference/ .
COPY proto ../proto

RUN mkdir -p generated && \
    python -m grpc_tools.protoc \
    -I../proto \
    --python_out=./generated \
    --grpc_python_out=./generated \
    ../proto/cogniflex/inference/*.proto

RUN python -c "from transformers import AutoTokenizer, AutoModel; \
    AutoTokenizer.from_pretrained('roberta-base'); \
    AutoModel.from_pretrained('roberta-base')"

CMD ["python", "main.py"]
