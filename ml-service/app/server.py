import logging
import os
import platform
import sys
import time
from concurrent import futures
from pathlib import Path

import grpc
import ml_analyzer_pb2_grpc

from app.config import load_settings
from app.detectors.mocks import MockTextDetector, MockVideoDetector
from app.detectors.photo_detector import MultitypePictureDetector
from app.grpc_service import MLAnalyzerServicer


def serve(service_root: Path, project_root: Path):
    settings = load_settings(service_root, project_root)
    max_message_length_bytes = settings.grpc.max_message_mb * 1024 * 1024

    photo_detector, video_detector, text_detector = _preload_models(settings)

    workers = min(32, (os.cpu_count() or 1) * 4)
    server = grpc.server(
        futures.ThreadPoolExecutor(max_workers=workers),
        options=[
            ("grpc.max_send_message_length", max_message_length_bytes),
            ("grpc.max_receive_message_length", max_message_length_bytes),
        ],
    )
    ml_analyzer_pb2_grpc.add_MLAnalyzerServicer_to_server(
        MLAnalyzerServicer(photo_detector, video_detector, text_detector),
        server,
    )
    server.add_insecure_port(f"{settings.grpc.host}:{settings.grpc.port}")

    logging.info("=" * 60)
    logging.info("ML Analyzer gRPC Server")
    logging.info("Python: %s", sys.version)
    logging.info("Platform: %s %s", platform.system(), platform.release())
    logging.info("Bind address: %s:%s", settings.grpc.host, settings.grpc.port)
    logging.info("Endpoints: AnalyzePhoto, AnalyzeVideo, AnalyzeText")
    logging.info("Workers: %s", workers)
    logging.info("Max message size: %s MB (%s bytes)", settings.grpc.max_message_mb, max_message_length_bytes)
    logging.info("Waiting for requests...")
    logging.info("=" * 60)

    server.start()

    try:
        server.wait_for_termination()
    except KeyboardInterrupt:
        logging.info("Shutting down server...")
        os._exit(0)


def _preload_models(settings):
    logging.info("Preloading all models...")
    start = time.time()

    with futures.ThreadPoolExecutor(max_workers=3) as executor:
        future_photo = executor.submit(_load_photo_model, settings)
        future_video = executor.submit(_load_video_model)
        future_text = executor.submit(_load_text_model, settings)

        photo_detector = future_photo.result()
        video_detector = future_video.result()
        text_detector = future_text.result()

    elapsed = time.time() - start
    logging.info("All models loaded in %.3fs", elapsed)
    return photo_detector, video_detector, text_detector


def _load_photo_model(settings):
    logging.info("Loading photo detection models...")
    detector = MultitypePictureDetector(
        path_general=str(settings.image_model.general_weights),
        path_faces=str(settings.image_model.faces_weights),
    )
    logging.info("Photo models loaded successfully!")
    return detector


def _load_video_model():
    logging.warning("USING MOCK VIDEO DETECTOR")
    return MockVideoDetector()


def _load_text_model(settings):
    from app.detectors.text_detector import TextDetector
    
    logging.info("Loading text detection model (RoBERTa)...")
    detector = TextDetector(
        model_weights_path=str(settings.text_model.model_weights),
        model_name=settings.text_model.model_name
    )
    logging.info("Text model loaded successfully!")
    logging.info("Warming up text model...")
    detector.predict_text("warmup")
    logging.info("Warmup done")
    return detector