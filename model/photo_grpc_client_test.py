import grpc
import sys
import os

current_dir = os.path.dirname(os.path.abspath(__file__))
generated_dir = os.path.join(current_dir, "generated")
sys.path.insert(0, generated_dir)

import photo_detector_pb2
import photo_detector_pb2_grpc

def test_image(image_path: str):
    if not os.path.exists(image_path):
        print(f"ERROR: File not found: {image_path}")
        return
    
    with open(image_path, "rb") as f:
        image_bytes = f.read()
    
    print(f"Sending {len(image_bytes)} bytes to Photo gRPC server...")
    
    channel = grpc.insecure_channel("localhost:50051")
    stub = photo_detector_pb2_grpc.PhotoDetectorStub(channel)
    
    try:
        response = stub.Detect(
            photo_detector_pb2.PhotoRequest(image_data=image_bytes),
            timeout=10.0
        )
        
        print("-" * 40)
        predicted_class = getattr(response, 'class')
        confidence = response.confidence
        print(f"CLASS:      {predicted_class}")
        print(f"CONFIDENCE: {confidence:.4f}")
        print("-" * 40)
        
    except grpc.RpcError as e:
        print(f"gRPC error: {e.code()} - {e.details()}")
    except Exception as e:
        print(f"Unexpected error: {e}")

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python photo_grpc_client_test.py <path_to_image>")
        sys.exit(1)
    
    test_image(sys.argv[1])