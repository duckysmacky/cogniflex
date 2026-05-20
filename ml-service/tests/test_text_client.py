import grpc
import sys
import os
from common import load_env_file

sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..', 'generated'))

import ml_analyzer_pb2
import ml_analyzer_pb2_grpc


def test_text(text):
    print(f"Sending text: {text[:50]}...")
    
    target = f"{os.getenv('ML_GRPC_HOST', 'localhost')}:{os.getenv('ML_GRPC_PORT', '50051')}"
    channel = grpc.insecure_channel(target)
    stub = ml_analyzer_pb2_grpc.MLAnalyzerStub(channel)
    
    try:
        response = stub.AnalyzeText(
            ml_analyzer_pb2.TextRequest(text=text),
            timeout=10.0
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
        print("Usage: python test_text_client.py \"your text here\"")
        sys.exit(1)
    
    load_env_file(os.path.join(os.path.dirname(__file__), "..", "..", ".env"))
    test_text(sys.argv[1])