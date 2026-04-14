# Cogniflex ML gRPC API

Документация по использованию gRPC сервиса для анализа изображений, видео и текста.

---

## Требования

- **Python 3.10** (обязательно, mediapipe не работает с Python 3.14)
- Установка зависимостей:
```bash
  py -3.10 -m pip install -r model/requirements.txt
```

## Запуск сервера

```bash
cd ml-service
py -3.10 server.py
```

Сервер запускается на порту **50051** и предоставляет три метода:

- `AnalyzePhoto` — анализ изображений
    
- `AnalyzeVideo` — анализ видео
    
- `AnalyzeText` — анализ текста

## Тестирование

>Выполнять в **другом** окне терминала, только после запуска сервера.

```bash
cd ml-service
# Фото
py -3.10 tests/test_photo_client.py "путь_к_изображению"
# Видео
py -3.10 tests/test_video_client.py "путь_к_видео"
# Текст
py -3.10 tests/test_text_client.py "текст для анализа"
```

## API

**Порт:** `50051`  
**Сервис:** `MLAnalyzer`

### AnalyzePhoto — Анализ изображения

|Параметр|Значение|
|---|---|
|Вход|`bytes image_data` (JPEG, PNG, WebP, до 100 МБ)|
|Выход|`class` (`"human"` \| `"ai"`), `confidence` (0.0 - 1.0)|
|Статус|Реальная модель (ResNet18 + MediaPipe)|

### AnalyzeVideo — Анализ видео

|Параметр|Значение|
|---|---|
|Вход|`bytes video_data` (MP4, AVI, MOV, до 100 МБ)|
|Выход|`class` (`"human"` \| `"ai"`), `confidence` (0.0 - 1.0)|
|Статус|Заглушка (случайные предсказания)|

### AnalyzeText — Анализ текста

| Параметр | Значение                                                |
| -------- | ------------------------------------------------------- |
| Вход     | `string text`                                           |
| Выход    | `class` (`"human"` \| `"ai"`), `confidence` (0.0 - 1.0) |
| Статус   | Заглушка (случайные предсказания)                       |

## Proto

```protobuf
syntax = "proto3";
package cogniflex.ml;
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

## Модели для фото

`MultitypePictureDetector` автоматически выбирает подходящую модель:

|Модель|Файл|Точность|Назначение|
|---|---|---|---|
|General|`resnet_general92.pth`|92%|Обычные изображения|
|Faces|`resnet_faces88.pth`|88%|Изображения с лицами|

Выбор модели происходит автоматически с помощью **MediaPipe Face Detection**.

---

## Генерация gRPC файлов

При изменении `ml_analyzer.proto` необходимо перегенерировать Python-код:

```bash
cd \cogniflex\ml-service
py -3.10 -m grpc_tools.protoc -I./proto --python_out=./generated --grpc_python_out=./generated ./proto/ml_analyzer.proto
```

## Лимиты на размер сообщений

Сервер и клиенты по умолчанию настроены на **100 МБ**.

### Изменение лимита на сервере

В файле `server.py` в функции `serve()`:

```python
server = grpc.server(
    futures.ThreadPoolExecutor(max_workers=10),
    options=[
        ('grpc.max_send_message_length', 200 * 1024 * 1024),     # 200 МБ
        ('grpc.max_receive_message_length', 200 * 1024 * 1024),  # 200 МБ
    ]
)
```

### Изменение лимита в клиенте

В тестовых файлах:

```python
channel = grpc.insecure_channel(
    "localhost:50051",
    options=[
        ('grpc.max_send_message_length', 200 * 1024 * 1024),
        ('grpc.max_receive_message_length', 200 * 1024 * 1024),
    ]
)
```

### Расчёт размера

|Размер|Формула|
|---|---|
|10 МБ|`10 * 1024 * 1024`|
|50 МБ|`50 * 1024 * 1024`|
|100 МБ|`100 * 1024 * 1024`|
|200 МБ|`200 * 1024 * 1024`|
|500 МБ|`500 * 1024 * 1024`|
|1 ГБ|`1024 * 1024 * 1024`|
...