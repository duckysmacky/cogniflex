import grpc
from concurrent import futures
import logging
import sys
import os
import tempfile
import random

current_dir = os.path.dirname(os.path.abspath(__file__))
generated_dir = os.path.join(current_dir, "generated")
utils_dir = os.path.join(current_dir, "utils")

sys.path.insert(0, generated_dir)
sys.path.insert(0, current_dir)
sys.path.insert(0, utils_dir)

import video_detector_pb2
import video_detector_pb2_grpc

_detector = None

def get_detector():
    global _detector
    if _detector is None:
        logging.warning("USING MOCK VIDEO DETECTOR - returning random predictions")
        class MockVideoDetector:
            def predict_video(self, path):
                pred = random.choice([0, 1])
                conf = random.uniform(0.5, 1.0)
                return conf, pred
        _detector = MockVideoDetector()
    return _detector

class VideoDetectorServicer(video_detector_pb2_grpc.VideoDetectorServicer):
    
    def Detect(self, request, context):
        video_bytes = request.video_data
        
        if not video_bytes:
            context.set_code(grpc.StatusCode.INVALID_ARGUMENT)
            context.set_details("Empty video bytes received")
            return video_detector_pb2.DetectReply()
        
        logging.info(f"Received video: {len(video_bytes)} bytes")
        
        with tempfile.NamedTemporaryFile(suffix=".mp4", delete=False) as tmp_file:
            tmp_file.write(video_bytes)
            tmp_path = tmp_file.name
        
        try:
            detector = get_detector()
            confidence, pred = detector.predict_video(tmp_path)
            predicted_class = "human" if pred == 0 else "ai"
            
            logging.info(f"Video prediction: {predicted_class} (confidence: {confidence:.4f})")
            
            reply = video_detector_pb2.DetectReply()
            setattr(reply, 'class', predicted_class)
            reply.confidence = float(confidence)
            return reply
            
        except Exception as e:
            error_msg = f"Video inference failed: {str(e)}"
            logging.error(error_msg)
            context.set_code(grpc.StatusCode.INTERNAL)
            context.set_details(error_msg)
            return video_detector_pb2.DetectReply()
            
        finally:
            if os.path.exists(tmp_path):
                os.unlink(tmp_path)

def serve():
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    video_detector_pb2_grpc.add_VideoDetectorServicer_to_server(VideoDetectorServicer(), server)
    server.add_insecure_port("[::]:50052")
    
    logging.info("=" * 50)
    logging.info("Video gRPC server started on port 50052")
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