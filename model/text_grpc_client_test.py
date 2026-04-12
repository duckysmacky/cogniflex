import grpc
import sys
import os

current_dir = os.path.dirname(os.path.abspath(__file__))
generated_dir = os.path.join(current_dir, "generated")
sys.path.insert(0, generated_dir)

import text_detector_pb2
import text_detector_pb2_grpc

def test_text(text: str):
    print(f"Sending text to Text gRPC server: {text[:50]}...")
    
    channel = grpc.insecure_channel("localhost:50053")
    stub = text_detector_pb2_grpc.TextDetectorStub(channel)
    
    try:
        response = stub.Detect(
            text_detector_pb2.TextRequest(text=text),
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
        print("Usage: python text_grpc_client_test.py \"your text here\"")
        sys.exit(1)
    
    test_text(sys.argv[1])