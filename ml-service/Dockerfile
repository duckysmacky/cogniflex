# 1. Базовый образ
FROM python:3.10-slim

# 2. Указываем рабочую директорию внутри контейнера
WORKDIR /app

# 3. Настраиваем переменные окружения
ENV PYTHONUNBUFFERED=1
ENV PORT=50051

# 4. Сначала копируем ТОЛЬКО requirements.txt
COPY requirements.txt .

# 5. Устанавливаем зависимости
RUN pip install --no-cache-dir --upgrade pip && \
    pip install --no-cache-dir -r requirements.txt

# 6. Теперь копируем весь остальной исходный код и папки
COPY . .

# 7. Генерация gRPC кода
RUN python -m grpc_tools.protoc \
    -I./proto \
    --python_out=. \
    --grpc_python_out=. \
    ./proto/*.proto

# 9. Открываем порт для gRPC
EXPOSE 50051

# 10. Запускаем главный питоновский файлик
CMD ["python", "server.py"]