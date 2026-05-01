import grpc
from concurrent import futures
import logging
import sys
import os
import tempfile
import random

current_dir = os.path.dirname(os.path.abspath(__file__))
generated_dir = os.path.join(current_dir, "generated")
utils_dir = os.path.join(os.path.dirname(current_dir), "model", "utils")
weights_dir = os.path.join(os.path.dirname(current_dir), "model", "weights")

sys.path.insert(0, generated_dir)
sys.path.insert(0, utils_dir)

import ml_analyzer_pb2
import ml_analyzer_pb2_grpc

_photo_detector = None

def get_photo_detector():
    global _photo_detector
    if _photo_detector is None:
        from model_selection_mediapipe import MultitypePictureDetector
        
        path_general = os.path.join(weights_dir, "resnet_general92.pth")
        path_faces = os.path.join(weights_dir, "resnet_faces88.pth")
        
        logging.info("Loading photo detection models...")
        logging.info(f"  General: {path_general}")
        logging.info(f"  Faces:   {path_faces}")
        
        _photo_detector = MultitypePictureDetector(
            path_general=path_general,
            path_faces=path_faces
        )
        logging.info("Photo models loaded successfully!")
    
    return _photo_detector

_video_detector = None

def get_video_detector():
    global _video_detector
    if _video_detector is None:
        logging.warning("USING MOCK VIDEO DETECTOR - returning random predictions")
        class MockVideoDetector:
            def predict_video(self, path):
                pred = random.choice([0, 1])
                conf = random.uniform(0.5, 1.0)
                return conf, pred
        _video_detector = MockVideoDetector()
    return _video_detector

_text_detector = None

def get_text_detector():
    global _text_detector
    if _text_detector is None:
        logging.warning("USING MOCK TEXT DETECTOR - returning random predictions")
        class MockTextDetector:
            def predict_text(self, text):
                pred = random.choice([0, 1])
                conf = random.uniform(0.5, 1.0)
                return conf, pred
        _text_detector = MockTextDetector()
    return _text_detector

class MLAnalyzerServicer(ml_analyzer_pb2_grpc.MLAnalyzerServicer):
    
    def AnalyzePhoto(self, request, context):
        image_bytes = request.image_data
        
        if not image_bytes:
            context.set_code(grpc.StatusCode.INVALID_ARGUMENT)
            context.set_details("Empty image bytes received")
            return ml_analyzer_pb2.AnalyzeReply()
        
        logging.info(f"[Photo] Received: {len(image_bytes)} bytes")
        
        with tempfile.NamedTemporaryFile(suffix=".jpg", delete=False) as tmp_file:
            tmp_file.write(image_bytes)
            tmp_path = tmp_file.name
        
        try:
            detector = get_photo_detector()
            confidence, pred = detector.predict_picture(tmp_path)
            predicted_class = "human" if pred == 0 else "ai"
            
            logging.info(f"[Photo] Result: {predicted_class} ({confidence:.4f})")
            
            reply = ml_analyzer_pb2.AnalyzeReply()
            setattr(reply, 'class', predicted_class)
            reply.confidence = float(confidence)
            return reply
            
        except Exception as e:
            error_msg = f"Photo inference failed: {str(e)}"
            logging.error(error_msg)
            context.set_code(grpc.StatusCode.INTERNAL)
            context.set_details(error_msg)
            return ml_analyzer_pb2.AnalyzeReply()
            
        finally:
            if os.path.exists(tmp_path):
                os.unlink(tmp_path)
    
    def AnalyzeVideo(self, request, context):
        video_bytes = request.video_data
        
        if not video_bytes:
            context.set_code(grpc.StatusCode.INVALID_ARGUMENT)
            context.set_details("Empty video bytes received")
            return ml_analyzer_pb2.AnalyzeReply()
        
        logging.info(f"[Video] Received: {len(video_bytes)} bytes")
        
        with tempfile.NamedTemporaryFile(suffix=".mp4", delete=False) as tmp_file:
            tmp_file.write(video_bytes)
            tmp_path = tmp_file.name
        
        try:
            detector = get_video_detector()
            confidence, pred = detector.predict_video(tmp_path)
            predicted_class = "human" if pred == 0 else "ai"
            
            logging.info(f"[Video] Result: {predicted_class} ({confidence:.4f})")
            
            reply = ml_analyzer_pb2.AnalyzeReply()
            setattr(reply, 'class', predicted_class)
            reply.confidence = float(confidence)
            return reply
            
        except Exception as e:
            error_msg = f"Video inference failed: {str(e)}"
            logging.error(error_msg)
            context.set_code(grpc.StatusCode.INTERNAL)
            context.set_details(error_msg)
            return ml_analyzer_pb2.AnalyzeReply()
            
        finally:
            if os.path.exists(tmp_path):
                os.unlink(tmp_path)
    
    def AnalyzeText(self, request, context):
        text = request.text
        
        if not text:
            context.set_code(grpc.StatusCode.INVALID_ARGUMENT)
            context.set_details("Empty text received")
            return ml_analyzer_pb2.AnalyzeReply()
        
        logging.info(f"[Text] Received: {text[:100]}...")
        
        try:
            detector = get_text_detector()
            confidence, pred = detector.predict_text(text)
            predicted_class = "human" if pred == 0 else "ai"
            
            logging.info(f"[Text] Result: {predicted_class} ({confidence:.4f})")
            
            reply = ml_analyzer_pb2.AnalyzeReply()
            setattr(reply, 'class', predicted_class)
            reply.confidence = float(confidence)
            return reply
            
        except Exception as e:
            error_msg = f"Text inference failed: {str(e)}"
            logging.error(error_msg)
            context.set_code(grpc.StatusCode.INTERNAL)
            context.set_details(error_msg)
            return ml_analyzer_pb2.AnalyzeReply()

def serve():
    server = grpc.server(
    futures.ThreadPoolExecutor(max_workers=10),
    options=[
        ('grpc.max_send_message_length', 100 * 1024 * 1024),
        ('grpc.max_receive_message_length', 100 * 1024 * 1024),
    ]
    )
    ml_analyzer_pb2_grpc.add_MLAnalyzerServicer_to_server(MLAnalyzerServicer(), server)
    server.add_insecure_port("[::]:50051")
    
    logging.info("=" * 50)
    logging.info("AnalyzePhoto, AnalyzeVideo, AnalyzeText")
    logging.info("Waiting for requests...")
    logging.info("=" * 50)
    
    server.start()
    server.wait_for_termination()

if __name__ == "__main__":
    logging.basicConfig(
        level=logging.INFO,
        format="%(asctime)s [%(levelname)s] %(message)s"
    )
    serve()