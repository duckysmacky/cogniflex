FROM python:3.10-slim

WORKDIR /app/ml-service

ENV PYTHONUNBUFFERED=1

COPY ml-service/requirements.txt .

RUN pip install --no-cache-dir --upgrade pip && \
    pip install --no-cache-dir -r requirements.txt

COPY ml-service/ .
COPY proto ../proto

RUN mkdir -p generated && \
    python -m grpc_tools.protoc \
    -I../proto \
    --python_out=./generated \
    --grpc_python_out=./generated \
    ../proto/*.proto

CMD ["python", "main.py"]
