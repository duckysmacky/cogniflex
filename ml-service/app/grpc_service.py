import logging
import os
import tempfile
import time
import threading

import grpc
import ml_analyzer_pb2
import ml_analyzer_pb2_grpc

_inference_lock = threading.Lock()


class MLAnalyzerServicer(ml_analyzer_pb2_grpc.MLAnalyzerServicer):
    def __init__(self, photo_detector, video_detector, text_detector):
        self.photo_detector = photo_detector
        self.video_detector = video_detector
        self.text_detector = text_detector

    def AnalyzePhoto(self, request, context):
        image_bytes = request.image_data

        logging.info("=" * 60)
        logging.info("[Photo] New request received")
        logging.info("[Photo] Peer: %s", context.peer())
        logging.info(
            "[Photo] Data size: %s bytes (%.2f MB)",
            len(image_bytes),
            len(image_bytes) / 1024 / 1024,
        )

        if not image_bytes:
            logging.warning("[Photo] Empty request received, returning error")
            context.set_code(grpc.StatusCode.INVALID_ARGUMENT)
            context.set_details("Empty image bytes received")
            return ml_analyzer_pb2.AnalyzeReply()

        with tempfile.NamedTemporaryFile(suffix=".jpg", delete=False) as tmp_file:
            tmp_file.write(image_bytes)
            tmp_path = tmp_file.name
        logging.info("[Photo] Saved to temp file: %s", tmp_path)

        start_time = time.time()

        try:
            with _inference_lock:
                confidence, pred = self.photo_detector.predict_picture(tmp_path)
            return _reply_from_prediction("Photo", start_time, confidence, pred)
        except Exception as e:
            _set_internal_error(context, "Photo", start_time, e)
            return ml_analyzer_pb2.AnalyzeReply()
        finally:
            if os.path.exists(tmp_path):
                os.unlink(tmp_path)
                logging.info("[Photo] Temp file deleted: %s", tmp_path)

    def AnalyzeVideo(self, request, context):
        video_bytes = request.video_data

        logging.info("=" * 60)
        logging.info("[Video] New request received")
        logging.info("[Video] Peer: %s", context.peer())
        logging.info(
            "[Video] Data size: %s bytes (%.2f MB)",
            len(video_bytes),
            len(video_bytes) / 1024 / 1024,
        )

        if not video_bytes:
            logging.warning("[Video] Empty request received, returning error")
            context.set_code(grpc.StatusCode.INVALID_ARGUMENT)
            context.set_details("Empty video bytes received")
            return ml_analyzer_pb2.AnalyzeReply()

        with tempfile.NamedTemporaryFile(suffix=".mp4", delete=False) as tmp_file:
            tmp_file.write(video_bytes)
            tmp_path = tmp_file.name
        logging.info("[Video] Saved to temp file: %s", tmp_path)

        start_time = time.time()

        try:
            with _inference_lock:
                confidence, pred = self.video_detector.predict_video(tmp_path)
            return _reply_from_prediction("Video", start_time, confidence, pred)
        except Exception as e:
            _set_internal_error(context, "Video", start_time, e)
            return ml_analyzer_pb2.AnalyzeReply()
        finally:
            if os.path.exists(tmp_path):
                os.unlink(tmp_path)
                logging.info("[Video] Temp file deleted: %s", tmp_path)

    def AnalyzeText(self, request, context):
        text = request.text

        logging.info("=" * 60)
        logging.info("[Text] New request received")
        logging.info("[Text] Peer: %s", context.peer())
        logging.info("[Text] Text length: %s chars", len(text))
        logging.info("[Text] Content: %s...", text[:200])

        if not text:
            logging.warning("[Text] Empty request received, returning error")
            context.set_code(grpc.StatusCode.INVALID_ARGUMENT)
            context.set_details("Empty text received")
            return ml_analyzer_pb2.AnalyzeReply()

        start_time = time.time()

        try:
            with _inference_lock:
                confidence, pred = self.text_detector.predict_text(text)
            return _reply_from_prediction("Text", start_time, confidence, pred)
        except Exception as e:
            _set_internal_error(context, "Text", start_time, e)
            return ml_analyzer_pb2.AnalyzeReply()


def _reply_from_prediction(operation, start_time, confidence, pred):
    predicted_class = "human" if pred == 0 else "ai"
    elapsed = time.time() - start_time

    logging.info("[%s] Inference completed in %.3fs", operation, elapsed)
    logging.info(
        "[%s] Result: class=%s, confidence=%.4f",
        operation,
        predicted_class,
        confidence,
    )

    reply = ml_analyzer_pb2.AnalyzeReply()
    setattr(reply, "class", predicted_class)
    reply.confidence = float(confidence)
    return reply


def _set_internal_error(context, operation, start_time, error):
    elapsed = time.time() - start_time
    error_msg = f"{operation} inference failed after {elapsed:.3f}s: {str(error)}"
    logging.error("[%s] %s", operation, error_msg)
    context.set_code(grpc.StatusCode.INTERNAL)
    context.set_details(error_msg)