import grpc
import sys
import os

sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..', 'generated'))

import ml_analyzer_pb2
import ml_analyzer_pb2_grpc

def test_photo(image_path):
    if not os.path.exists(image_path):
        print(f"ERROR: File not found: {image_path}")
        return
    
    with open(image_path, "rb") as f:
        image_bytes = f.read()
    
    print(f"Sending {len(image_bytes)} bytes to Photo analyzer...")
    
    channel = grpc.insecure_channel("localhost:50051")
    stub = ml_analyzer_pb2_grpc.MLAnalyzerStub(channel)
    
    try:
        response = stub.AnalyzePhoto(
            ml_analyzer_pb2.PhotoRequest(image_data=image_bytes),
            timeout=30.0
        )
        
        print("-" * 40)
        print(f"CLASS:      {getattr(response, 'class')}")
        print(f"CONFIDENCE: {response.confidence:.4f}")
        print("-" * 40)
        
    except grpc.RpcError as e:
        print(f"gRPC error: {e.code()} - {e.details()}")
    except Exception as e:
        print(f"Unexpected error: {e}")

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python test_photo_client.py <path_to_image>")
        sys.exit(1)
    
    test_photo(sys.argv[1])