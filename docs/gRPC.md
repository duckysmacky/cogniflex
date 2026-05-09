# Cogniflex ML gRPC API

  
Документация по использованию gRPC сервиса для анализа изображений, видео и текста.

  
---

  
## Требования

- Установка зависимостей:

```bash
  py -3.10 -m pip install -r ml-service/requirements.txt
```

## Запуск сервера

  
```bash
cd ml-service
python server.py
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
python tests/test_photo_client.py "путь_к_изображению"
# Видео
python tests/test_video_client.py "путь_к_видео"
# Текст
python tests/test_text_client.py "текст для анализа"
```
  

### AnalyzePhoto — Анализ изображения


Параметр | Значение
Вход | `bytes image_data` (JPEG, PNG, WebP, до 100 МБ)
Выход | `class` (`"human"` \| `"ai"`), `confidence` (0.0 - 1.0)
Статус | Реальная модель (ResNet18 + OpenCV Haar Cascade)
  
### AnalyzeVideo — Анализ видео

Параметр | Значение
Вход | `bytes video_data` (MP4, AVI, MOV, до 100 МБ)
Выход | `class` (`"human"` \| `"ai"`), `confidence` (0.0 - 1.0)
Статус | Заглушка (случайные предсказания)
  
### AnalyzeText — Анализ текста

Параметр | Значение
Вход | `string text`
Выход | `class` (`"human"` \| `"ai"`), `confidence` (0.0 - 1.0)
Статус | Заглушка (случайные предсказания)
  
## Логирование
  
Сервер выводит подробные логи о каждом запросе. Формат логов:
  
#### Запуск сервера

```bash
============================================================
[INFO] ML Analyzer gRPC Server
[INFO] Python: sys.version
[INFO] Platform: {platform.system()} {platform.release()}
[INFO] Port: 50051
[INFO] Endpoints: AnalyzePhoto, AnalyzeVideo, AnalyzeText
[INFO] Max message size: 100 MB (104857600 bytes)
[INFO] Waiting for requests...
============================================================
```

#### Входящий запрос (на примере фото)

```bash
============================================================
[INFO] [Photo] New request received
[INFO] [Photo] Peer: ipv4:127.0.0.1:54321
[INFO] [Photo] Data size: 1424924 bytes (1.36 MB)
[INFO] [Photo] Saved to temp file: C:\Users\...\tmp123.jpg
[INFO] [Photo] Inference completed in 0.432s
[INFO] [Photo] Result: class=ai, confidence=0.9969
[INFO] [Photo] Temp file deleted: C:\Users\...\tmp123.jpg
```
  
### Что означают поля логов
  
[Photo/Video/Text] - Тип запроса
Peer - Адрес клиента, отправившего запрос
Data size - Размер полученных данных в байтах и МБ
Saved to temp file - Путь к временному файлу
Inference completed in - Время выполнения анализа
Result - Итоговый класс и уверенность
Temp file deleted - Подтверждение удаления временного файла
  

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
  
Модель | Файл | Точность | Назначение
General | `resnet_general92.pth` | 92% | Обычные изображения
Faces | `resnet_faces88.pth` | 88% | Изображения с лицами  
Выбор модели происходит автоматически с помощью **OpenCV Haar Cascade Face Detection**.

---  

## Лимиты на размер сообщений

Сервер и клиенты по умолчанию настроены на **100 МБ**.

### Изменение лимита на сервере

В файле ml-service/config.yaml :

```python
grpc:
  port: 50051
  max_message_mb: 100
```