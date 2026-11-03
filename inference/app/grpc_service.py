import logging
import os
import tempfile
import time
import threading
from typing import TYPE_CHECKING

import grpc
from cogniflex.inference import analyzer_pb2, analyzer_pb2_grpc

if TYPE_CHECKING:
    from app.detectors.photo_detector import MultitypePictureDetector
    from app.detectors.text_detector import TextDetector

_inference_lock = threading.Lock()

VIDEO_UNSUPPORTED_DETAILS = "Video analysis is not supported yet"


class AnalyzerServicer(analyzer_pb2_grpc.AnalyzerServicer):
    def __init__(
        self,
        photo_detector: "MultitypePictureDetector",
        text_detector: "TextDetector",
    ) -> None:
        self.photo_detector = photo_detector
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
            return analyzer_pb2.AnalyzeReply()

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
            return analyzer_pb2.AnalyzeReply()
        finally:
            if os.path.exists(tmp_path):
                os.unlink(tmp_path)
                logging.info("[Photo] Temp file deleted: %s", tmp_path)

    def AnalyzeVideo(
        self,
        request: analyzer_pb2.VideoRequest,
        context: grpc.ServicerContext,
    ) -> analyzer_pb2.AnalyzeReply:
        # No real video model exists yet; never fabricate a verdict.
        logging.warning("[Video] Request rejected: video analysis is not implemented")
        context.set_code(grpc.StatusCode.UNIMPLEMENTED)
        context.set_details(VIDEO_UNSUPPORTED_DETAILS)
        return analyzer_pb2.AnalyzeReply()

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
            return analyzer_pb2.AnalyzeReply()

        start_time = time.time()

        try:
            with _inference_lock:
                confidence, pred = self.text_detector.predict_text(text)
            return _reply_from_prediction("Text", start_time, confidence, pred)
        except Exception as e:
            _set_internal_error(context, "Text", start_time, e)
            return analyzer_pb2.AnalyzeReply()


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

    reply = analyzer_pb2.AnalyzeReply()
    setattr(reply, "class", predicted_class)
    reply.confidence = float(confidence)
    return reply


def _set_internal_error(
    context: grpc.ServicerContext,
    operation: str,
    start_time: float,
    error: Exception,
) -> None:
    elapsed = time.time() - start_time
    error_msg = f"{operation} inference failed after {elapsed:.3f}s: {str(error)}"
    logging.error("[%s] %s", operation, error_msg)
    context.set_code(grpc.StatusCode.INTERNAL)
    context.set_details(error_msg)
