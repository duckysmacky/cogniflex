import grpc
from concurrent import futures
import logging
import sys
import os
import tempfile

current_dir = os.path.dirname(os.path.abspath(__file__))
generated_dir = os.path.join(current_dir, "generated")
utils_dir = os.path.join(current_dir, "utils")

sys.path.insert(0, generated_dir)
sys.path.insert(0, current_dir)
sys.path.insert(0, utils_dir)

import detector_pb2
import detector_pb2_grpc
from model_selection_mediapipe import MultitypePictureDetector

_detector = None

def get_detector():
    global _detector
    if _detector is None:
        path_general = os.path.join(current_dir, "weights", "resnet_general92.pth")
        path_faces = os.path.join(current_dir, "weights", "resnet_faces88.pth")
        
        logging.info(f"Loading model from:")
        logging.info(f"  General: {path_general}")
        logging.info(f"  Faces:   {path_faces}")
        
        _detector = MultitypePictureDetector(
            path_general=path_general,
            path_faces=path_faces
        )
        logging.info("Model loaded successfully!")
    
    return _detector

class DetectorServicer(detector_pb2_grpc.ImageDetectorServicer):
    
    def Detect(self, request, context):
        image_bytes = request.image_data
        
        if not image_bytes:
            context.set_code(grpc.StatusCode.INVALID_ARGUMENT)
            context.set_details("Empty image bytes received")
            return detector_pb2.DetectReply()
        
        logging.info(f"Received image: {len(image_bytes)} bytes")
        
        with tempfile.NamedTemporaryFile(suffix=".jpg", delete=False) as tmp_file:
            tmp_file.write(image_bytes)
            tmp_path = tmp_file.name
        
        try:
            detector = get_detector()
            confidence, pred = detector.predict_picture(tmp_path)
            predicted_class = "human" if pred == 0 else "ai"
            
            logging.info(f"Prediction: {predicted_class} (confidence: {confidence:.4f})")
            
            return detector_pb2.DetectReply(
                class_=predicted_class,
                confidence=float(confidence)
            )
            
        except Exception as e:
            error_msg = f"Model inference failed: {str(e)}"
            logging.error(error_msg)
            context.set_code(grpc.StatusCode.INTERNAL)
            context.set_details(error_msg)
            return detector_pb2.DetectReply()
            
        finally:
            if os.path.exists(tmp_path):
                os.unlink(tmp_path)

def serve():
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    detector_pb2_grpc.add_ImageDetectorServicer_to_server(DetectorServicer(), server)
    server.add_insecure_port("[::]:50051")
    
    logging.info("=" * 50)
    logging.info("gRPC server started on port 50051")
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