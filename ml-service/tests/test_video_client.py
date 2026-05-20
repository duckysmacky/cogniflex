import grpc
import sys
import os
import yaml
from common import load_env_file

sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..', 'generated'))

import ml_analyzer_pb2
import ml_analyzer_pb2_grpc


def test_video(video_path):
    if not os.path.exists(video_path):
        print(f"ERROR: File not found: {video_path}")
        return
    
    with open(video_path, "rb") as f:
        video_bytes = f.read()
    
    print(f"Sending {len(video_bytes)} bytes to Video analyzer...")
    
    config_path = os.path.join(os.path.dirname(__file__), "..", "config.yaml")
    with open(config_path, "r", encoding="utf-8") as f:
        config = yaml.safe_load(f)

    target = f"{os.getenv('ML_GRPC_HOST', 'localhost')}:{os.getenv('ML_GRPC_PORT', '50051')}"
    max_message_length_bytes = int(config["grpc"].get("max_message_mb", 100)) * 1024 * 1024
    channel = grpc.insecure_channel(
        target,
        options=[
            ('grpc.max_send_message_length', max_message_length_bytes),
            ('grpc.max_receive_message_length', max_message_length_bytes),
        ]
    )
    stub = ml_analyzer_pb2_grpc.MLAnalyzerStub(channel)
    
    try:
        response = stub.AnalyzeVideo(
            ml_analyzer_pb2.VideoRequest(video_data=video_bytes),
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
        print("Usage: python test_video_client.py <path_to_video>")
        sys.exit(1)
    
    load_env_file(os.path.join(os.path.dirname(__file__), "..", "..", ".env"))
    test_video(sys.argv[1])