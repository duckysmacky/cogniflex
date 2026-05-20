# Cogniflex ML gRPC API

Документация по gRPC-сервису ML-инференса для анализа изображений, видео и текста.

## Где находится контракт

Единый protobuf-контракт хранится в корне репозитория:

```text
proto/ml_analyzer.proto
```

Этот файл является единственным источником правды для Backend и ML-сервиса:

- Backend генерирует Java gRPC-клиент из `proto/`.
- ML-сервис генерирует Python gRPC-код из `proto/` в `ml-service/generated/`.

Сгенерированные файлы не коммитятся.

## Структура ML-сервиса

```text
ml-service/
  main.py
  config.yaml
  requirements.txt
  app/
    config.py
    proto.py
    server.py
    grpc_service.py
    detectors/
    inference/
  generated/
  weights/
```

- `main.py` - точка входа сервиса.
- `app/proto.py` - генерация Python gRPC-кода из `proto/ml_analyzer.proto`.
- `app/server.py` - запуск gRPC-сервера и загрузка моделей.
- `app/grpc_service.py` - реализация RPC-методов.
- `app/detectors/` - runtime-обертки над моделями.
- `app/inference/` - runtime-предобработка.
- `weights/` - локальные веса моделей для ML-сервиса. Файлы весов не коммитятся.

## Требования

Установка зависимостей для локального запуска ML-сервиса:

```bash
py -3.10 -m pip install -r ml-service/requirements.txt
```

Для анализа изображений нужны веса, указанные в `ml-service/config.yaml`.
По умолчанию ожидаются:

```text
ml-service/weights/resnet_general92.pth
ml-service/weights/resnet_faces88.pth
```

## Конфигурация

Сетевые параметры задаются через `.env`:

```env
GRPC_HOST=[::]
GRPC_PORT=50051
```

Параметры самого сервиса задаются в `ml-service/config.yaml`:

```yaml
app:
  max_message_mb: 100

models:
  image:
    general_weights: weights/resnet_general92.pth
    faces_weights: weights/resnet_faces88.pth
```

Пути к весам относительные от директории `ml-service/`.

## Запуск сервера

Локально:

```bash
cd ml-service
python main.py
```

Через Docker Compose:

```bash
docker compose up --build ml-service
```

Сервис запускает gRPC-сервер на `GRPC_HOST:GRPC_PORT` и предоставляет методы:

- `AnalyzePhoto` - анализ изображений.
- `AnalyzeVideo` - анализ видео.
- `AnalyzeText` - анализ текста.

## Тестирование

Выполнять в другом окне терминала после запуска сервера.

```bash
cd ml-service

python tests/test_photo_client.py "путь_к_изображению"
python tests/test_video_client.py "путь_к_видео"
python tests/test_text_client.py "текст для анализа"
```

Если клиент запускается с хоста, адрес должен указывать на доступный с хоста gRPC endpoint.
При локальном запуске обычно используется `localhost:50051`.

## Методы

### AnalyzePhoto

| Параметр | Значение |
| --- | --- |
| Вход | `bytes image_data` |
| Поддерживаемые форматы | JPEG, PNG, WebP |
| Выход | `class` (`"human"` или `"ai"`), `confidence` (`0.0` - `1.0`) |
| Статус | Реальная модель ResNet18 + OpenCV Haar Cascade |

`MultitypePictureDetector` автоматически выбирает модель:

| Модель | Файл | Назначение |
| --- | --- | --- |
| General | `resnet_general92.pth` | Обычные изображения |
| Faces | `resnet_faces88.pth` | Изображения с лицами |

### AnalyzeVideo

| Параметр | Значение |
| --- | --- |
| Вход | `bytes video_data` |
| Поддерживаемые форматы | MP4, AVI, MOV |
| Выход | `class` (`"human"` или `"ai"`), `confidence` (`0.0` - `1.0`) |
| Статус | Заглушка, возвращает случайные предсказания |

### AnalyzeText

| Параметр | Значение |
| --- | --- |
| Вход | `string text` |
| Выход | `class` (`"human"` или `"ai"`), `confidence` (`0.0` - `1.0`) |
| Статус | Заглушка, возвращает случайные предсказания |

## Лимит размера сообщений

Сервер по умолчанию принимает сообщения до `100 МБ`.

Лимит настраивается в `ml-service/config.yaml`:

```yaml
app:
  max_message_mb: 100
```

## Логирование

При запуске сервис пишет параметры окружения и доступные endpoints:

```text
============================================================
[INFO] ML Analyzer gRPC Server
[INFO] Python: ...
[INFO] Platform: ...
[INFO] Bind address: [::]:50051
[INFO] Endpoints: AnalyzePhoto, AnalyzeVideo, AnalyzeText
[INFO] Max message size: 100 MB (104857600 bytes)
[INFO] Waiting for requests...
============================================================
```

Пример логов входящего запроса:

```text
============================================================
[INFO] [Photo] New request received
[INFO] [Photo] Peer: ipv4:127.0.0.1:54321
[INFO] [Photo] Data size: 1424924 bytes (1.36 MB)
[INFO] [Photo] Saved to temp file: /tmp/tmp123.jpg
[INFO] [Photo] Inference completed in 0.432s
[INFO] [Photo] Result: class=ai, confidence=0.9969
[INFO] [Photo] Temp file deleted: /tmp/tmp123.jpg
```

## Proto

Актуальная версия находится в `proto/ml_analyzer.proto`:

```protobuf
syntax = "proto3";

package cogniflex.ml;

option java_multiple_files = true;
option java_package = "io.github.duckysmacky.cogniflex.grpc";
option java_outer_classname = "MlAnalyzerProto";

service MLAnalyzer {
  rpc AnalyzePhoto (PhotoRequest) returns (AnalyzeReply);
  rpc AnalyzeVideo (VideoRequest) returns (AnalyzeReply);
  rpc AnalyzeText (TextRequest) returns (AnalyzeReply);
}

message PhotoRequest {
  bytes image_data = 1;
}

message VideoRequest {
  bytes video_data = 1;
}

message TextRequest {
  string text = 1;
}

message AnalyzeReply {
  string class = 1;
  float confidence = 2;
}
```