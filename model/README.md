Cogniflex ML gRPC Servers - Документация

ТРЕБОВАНИЯ
- Python 3.10 (обязательно, mediapipe не работает с Python 3.14)
- Установка зависимостей: py -3.10 -m pip install -r requirements.txt

ЗАПУСК СЕРВЕРОВ
Фото (порт 50051): py -3.10 photo_grpc_server.py
Видео (порт 50052): py -3.10 video_grpc_server.py
Текст (порт 50053): py -3.10 text_grpc_server.py

API: PHOTO DETECTOR (ФОТО)
Порт: 50051
Сервис: PhotoDetector
Метод: Detect(PhotoRequest) returns (DetectReply)
Вход: bytes image_data (JPEG, PNG, WebP)
Выход: class ("human" или "ai"), confidence (0.0 - 1.0)
Статус: Реальная модель (ResNet18 + MediaPipe)

Proto:
service PhotoDetector {
  rpc Detect (PhotoRequest) returns (DetectReply);
}
message PhotoRequest {
  bytes image_data = 1;
}
message DetectReply {
  string class = 1;
  float confidence = 2;
}

API: VIDEO DETECTOR (ВИДЕО)
Порт: 50052
Сервис: VideoDetector
Метод: Detect(VideoRequest) returns (DetectReply)
Вход: bytes video_data (MP4, AVI, MOV)
Выход: class ("human" или "ai"), confidence (0.0 - 1.0)
Статус: Заглушка (случайные предсказания)

API: TEXT DETECTOR (ТЕКСТ)
Порт: 50053
Сервис: TextDetector
Метод: Detect(TextRequest) returns (DetectReply)
Вход: string text
Выход: class ("human" или "ai"), confidence (0.0 - 1.0)
Статус: Заглушка (случайные предсказания)

ТЕСТИРОВАНИЕ
Фото: py -3.10 photo_grpc_client_test.py "путь_к_изображению"
Видео: py -3.10 video_grpc_client_test.py "путь_к_видео"
Текст: py -3.10 text_grpc_client_test.py "текст для анализа"

МОДЕЛИ ДЛЯ ФОТО
MultitypePictureDetector автоматически выбирает модель:
- resnet_general92.pth (92% точность) - для обычных изображений
- resnet_faces88.pth (88% точность) - для изображений с лицами
Выбор происходит с помощью MediaPipe Face Detection.

ГЕНЕРАЦИЯ GRPC ФАЙЛОВ
При изменении .proto файлов:
cd model
py -3.10 -m grpc_tools.protoc -I./protos --python_out=./generated --grpc_python_out=./generated ./protos/photo_detector.proto
py -3.10 -m grpc_tools.protoc -I./protos --python_out=./generated --grpc_python_out=./generated ./protos/video_detector.proto
py -3.10 -m grpc_tools.protoc -I./protos --python_out=./generated --grpc_python_out=./generated ./protos/text_detector.proto