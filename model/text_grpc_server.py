import grpc
from concurrent import futures
import logging
import sys
import os
import random

current_dir = os.path.dirname(os.path.abspath(__file__))
generated_dir = os.path.join(current_dir, "generated")
utils_dir = os.path.join(current_dir, "utils")

sys.path.insert(0, generated_dir)
sys.path.insert(0, current_dir)
sys.path.insert(0, utils_dir)

import text_detector_pb2
import text_detector_pb2_grpc

_detector = None

def get_detector():
    global _detector
    if _detector is None:
        logging.warning("USING MOCK TEXT DETECTOR - returning random predictions")
        class MockTextDetector:
            def predict_text(self, text):
                pred = random.choice([0, 1])
                conf = random.uniform(0.5, 1.0)
                return conf, pred
        _detector = MockTextDetector()
    return _detector

class TextDetectorServicer(text_detector_pb2_grpc.TextDetectorServicer):
    
    def Detect(self, request, context):
        text = request.text
        
        if not text:
            context.set_code(grpc.StatusCode.INVALID_ARGUMENT)
            context.set_details("Empty text received")
            return text_detector_pb2.DetectReply()
        
        logging.info(f"Received text: {text[:100]}...")
        
        try:
            detector = get_detector()
            confidence, pred = detector.predict_text(text)
            predicted_class = "human" if pred == 0 else "ai"
            
            logging.info(f"Text prediction: {predicted_class} (confidence: {confidence:.4f})")
            
            reply = text_detector_pb2.DetectReply()
            setattr(reply, 'class', predicted_class)
            reply.confidence = float(confidence)
            return reply
            
        except Exception as e:
            error_msg = f"Text inference failed: {str(e)}"
            logging.error(error_msg)
            context.set_code(grpc.StatusCode.INTERNAL)
            context.set_details(error_msg)
            return text_detector_pb2.DetectReply()

def serve():
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    text_detector_pb2_grpc.add_TextDetectorServicer_to_server(TextDetectorServicer(), server)
    server.add_insecure_port("[::]:50053")
    
    logging.info("=" * 50)
    logging.info("Text gRPC server started on port 50053")
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