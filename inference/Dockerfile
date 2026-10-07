FROM python:3.10-slim

WORKDIR /app/inference

ARG HF_TOKEN

ENV PYTHONUNBUFFERED=1
ENV HF_TOKEN=${HF_TOKEN}

COPY inference/requirements.txt .

RUN pip install --no-cache-dir --upgrade pip && \
    pip install --no-cache-dir -r requirements.txt

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