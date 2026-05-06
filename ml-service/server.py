import grpc
from concurrent import futures
import logging
import sys
import os
import tempfile
import random
import time
import subprocess
import platform
import configparser

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(message)s"
)

current_dir = os.path.dirname(os.path.abspath(__file__))
project_root = os.path.dirname(current_dir)
generated_dir = os.path.join(current_dir, "generated")
proto_dir = os.path.join(current_dir, "proto")
utils_dir = os.path.join(project_root, "model", "utils")
weights_dir = os.path.join(project_root, "model", "weights")

sys.path.insert(0, generated_dir)
sys.path.insert(0, current_dir)
sys.path.insert(0, utils_dir)
sys.path.insert(0, project_root)

config = configparser.ConfigParser()
config.read(os.path.join(current_dir, 'config.cfg'))

GRPC_PORT = config.get('grpc', 'port', fallback='50051')
GRPC_MAX_MESSAGE_MB = config.getint('grpc', 'max_message_mb', fallback=100)

def generate_proto():
    proto_file = os.path.join(proto_dir, "ml_analyzer.proto")
    if not os.path.exists(proto_file):
        logging.error(f"Proto file not found: {proto_file}")
        return False
    
    os.makedirs(generated_dir, exist_ok=True)
    
    pb2_file = os.path.join(generated_dir, "ml_analyzer_pb2.py")
    pb2_grpc_file = os.path.join(generated_dir, "ml_analyzer_pb2_grpc.py")
    
    if os.path.exists(pb2_file) and os.path.exists(pb2_grpc_file):
        proto_mtime = os.path.getmtime(proto_file)
        pb2_mtime = os.path.getmtime(pb2_file)
        if pb2_mtime > proto_mtime:
            logging.info("Proto files are up to date, skipping generation")
            return True
    
    logging.info("Generating gRPC files from proto...")
    try:
        subprocess.run([
            sys.executable, "-m", "grpc_tools.protoc",
            f"-I{proto_dir}",
            f"--python_out={generated_dir}",
            f"--grpc_python_out={generated_dir}",
            proto_file
        ], check=True)
        logging.info("gRPC files generated successfully")
        return True
    except subprocess.CalledProcessError as e:
        logging.error(f"Failed to generate gRPC files: {e}")
        return False

if not generate_proto():
    logging.error("Cannot start server without generated proto files")
    sys.exit(1)

import ml_analyzer_pb2
import ml_analyzer_pb2_grpc

from model_selection_mediapipe import MultitypePictureDetector

_photo_detector = None
_video_detector = None
_text_detector = None

def load_photo_model():
    global _photo_detector
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

def load_video_model():
    global _video_detector
    logging.warning("USING MOCK VIDEO DETECTOR - returning random predictions")
    class MockVideoDetector:
        def predict_video(self, path):
            pred = random.choice([0, 1])
            conf = random.uniform(0.5, 1.0)
            return conf, pred
    _video_detector = MockVideoDetector()
    logging.info("Video detector ready (mock)")

def load_text_model():
    global _text_detector
    logging.warning("USING MOCK TEXT DETECTOR - returning random predictions")
    class MockTextDetector:
        def predict_text(self, text):
            pred = random.choice([0, 1])
            conf = random.uniform(0.5, 1.0)
            return conf, pred
    _text_detector = MockTextDetector()
    logging.info("Text detector ready (mock)")

def preload_models():
    logging.info("Preloading all models...")
    start = time.time()
    
    with futures.ThreadPoolExecutor(max_workers=3) as executor:
        future_photo = executor.submit(load_photo_model)
        future_video = executor.submit(load_video_model)
        future_text = executor.submit(load_text_model)
        
        futures_list = [future_photo, future_video, future_text]
        for f in futures.as_completed(futures_list):
            f.result()
    
    elapsed = time.time() - start
    logging.info(f"All models loaded in {elapsed:.3f}s")

class MLAnalyzerServicer(ml_analyzer_pb2_grpc.MLAnalyzerServicer):
    
    def AnalyzePhoto(self, request, context):
        image_bytes = request.image_data
        
        logging.info("=" * 60)
        logging.info("[Photo] New request received")
        logging.info(f"[Photo] Peer: {context.peer()}")
        logging.info(f"[Photo] Data size: {len(image_bytes)} bytes ({len(image_bytes) / 1024 / 1024:.2f} MB)")
        
        if not image_bytes:
            logging.warning("[Photo] Empty request received, returning error")
            context.set_code(grpc.StatusCode.INVALID_ARGUMENT)
            context.set_details("Empty image bytes received")
            return ml_analyzer_pb2.AnalyzeReply()
        
        with tempfile.NamedTemporaryFile(suffix=".jpg", delete=False) as tmp_file:
            tmp_file.write(image_bytes)
            tmp_path = tmp_file.name
        logging.info(f"[Photo] Saved to temp file: {tmp_path}")
        
        start_time = time.time()
        
        try:
            confidence, pred = _photo_detector.predict_picture(tmp_path)
            predicted_class = "human" if pred == 0 else "ai"
            elapsed = time.time() - start_time
            
            logging.info(f"[Photo] Inference completed in {elapsed:.3f}s")
            logging.info(f"[Photo] Result: class={predicted_class}, confidence={confidence:.4f}")
            
            reply = ml_analyzer_pb2.AnalyzeReply()
            setattr(reply, 'class', predicted_class)
            reply.confidence = float(confidence)
            return reply
            
        except Exception as e:
            elapsed = time.time() - start_time
            error_msg = f"Photo inference failed after {elapsed:.3f}s: {str(e)}"
            logging.error(f"[Photo] {error_msg}")
            context.set_code(grpc.StatusCode.INTERNAL)
            context.set_details(error_msg)
            return ml_analyzer_pb2.AnalyzeReply()
            
        finally:
            if os.path.exists(tmp_path):
                os.unlink(tmp_path)
                logging.info(f"[Photo] Temp file deleted: {tmp_path}")
    
    def AnalyzeVideo(self, request, context):
        video_bytes = request.video_data
        
        logging.info("=" * 60)
        logging.info("[Video] New request received")
        logging.info(f"[Video] Peer: {context.peer()}")
        logging.info(f"[Video] Data size: {len(video_bytes)} bytes ({len(video_bytes) / 1024 / 1024:.2f} MB)")
        
        if not video_bytes:
            logging.warning("[Video] Empty request received, returning error")
            context.set_code(grpc.StatusCode.INVALID_ARGUMENT)
            context.set_details("Empty video bytes received")
            return ml_analyzer_pb2.AnalyzeReply()
        
        with tempfile.NamedTemporaryFile(suffix=".mp4", delete=False) as tmp_file:
            tmp_file.write(video_bytes)
            tmp_path = tmp_file.name
        logging.info(f"[Video] Saved to temp file: {tmp_path}")
        
        start_time = time.time()
        
        try:
            confidence, pred = _video_detector.predict_video(tmp_path)
            predicted_class = "human" if pred == 0 else "ai"
            elapsed = time.time() - start_time
            
            logging.info(f"[Video] Inference completed in {elapsed:.3f}s")
            logging.info(f"[Video] Result: class={predicted_class}, confidence={confidence:.4f}")
            
            reply = ml_analyzer_pb2.AnalyzeReply()
            setattr(reply, 'class', predicted_class)
            reply.confidence = float(confidence)
            return reply
            
        except Exception as e:
            elapsed = time.time() - start_time
            error_msg = f"Video inference failed after {elapsed:.3f}s: {str(e)}"
            logging.error(f"[Video] {error_msg}")
            context.set_code(grpc.StatusCode.INTERNAL)
            context.set_details(error_msg)
            return ml_analyzer_pb2.AnalyzeReply()
            
        finally:
            if os.path.exists(tmp_path):
                os.unlink(tmp_path)
                logging.info(f"[Video] Temp file deleted: {tmp_path}")
    
    def AnalyzeText(self, request, context):
        text = request.text
        
        logging.info("=" * 60)
        logging.info("[Text] New request received")
        logging.info(f"[Text] Peer: {context.peer()}")
        logging.info(f"[Text] Text length: {len(text)} chars")
        logging.info(f"[Text] Content: {text[:200]}...")
        
        if not text:
            logging.warning("[Text] Empty request received, returning error")
            context.set_code(grpc.StatusCode.INVALID_ARGUMENT)
            context.set_details("Empty text received")
            return ml_analyzer_pb2.AnalyzeReply()
        
        start_time = time.time()
        
        try:
            confidence, pred = _text_detector.predict_text(text)
            predicted_class = "human" if pred == 0 else "ai"
            elapsed = time.time() - start_time
            
            logging.info(f"[Text] Inference completed in {elapsed:.3f}s")
            logging.info(f"[Text] Result: class={predicted_class}, confidence={confidence:.4f}")
            
            reply = ml_analyzer_pb2.AnalyzeReply()
            setattr(reply, 'class', predicted_class)
            reply.confidence = float(confidence)
            return reply
            
        except Exception as e:
            elapsed = time.time() - start_time
            error_msg = f"Text inference failed after {elapsed:.3f}s: {str(e)}"
            logging.error(f"[Text] {error_msg}")
            context.set_code(grpc.StatusCode.INTERNAL)
            context.set_details(error_msg)
            return ml_analyzer_pb2.AnalyzeReply()

def serve():
    max_message_length_bytes = GRPC_MAX_MESSAGE_MB * 1024 * 1024
    
    preload_models()
    
    server = grpc.server(
        futures.ThreadPoolExecutor(max_workers=10),
        options=[
            ('grpc.max_send_message_length', max_message_length_bytes),
            ('grpc.max_receive_message_length', max_message_length_bytes),
        ]
    )
    ml_analyzer_pb2_grpc.add_MLAnalyzerServicer_to_server(MLAnalyzerServicer(), server)
    server.add_insecure_port(f"[::]:{GRPC_PORT}")
    
    logging.info("=" * 60)
    logging.info("ML Analyzer gRPC Server")
    logging.info(f"Python: {sys.version}")
    logging.info(f"Platform: {platform.system()} {platform.release()}")
    logging.info(f"Port: {GRPC_PORT}")
    logging.info(f"Endpoints: AnalyzePhoto, AnalyzeVideo, AnalyzeText")
    logging.info(f"Max message size: {GRPC_MAX_MESSAGE_MB} MB ({max_message_length_bytes} bytes)")
    logging.info("Waiting for requests...")
    logging.info("=" * 60)
    
    server.start()
    server.wait_for_termination()

if __name__ == "__main__":
    serve()