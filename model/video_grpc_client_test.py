import grpc
import sys
import os

current_dir = os.path.dirname(os.path.abspath(__file__))
generated_dir = os.path.join(current_dir, "generated")
sys.path.insert(0, generated_dir)

import video_detector_pb2
import video_detector_pb2_grpc

def test_video(video_path: str):
    if not os.path.exists(video_path):
        print(f"ERROR: File not found: {video_path}")
        return
    
    with open(video_path, "rb") as f:
        video_bytes = f.read()
    
    print(f"Sending {len(video_bytes)} bytes to Video gRPC server...")
    
    channel = grpc.insecure_channel("localhost:50052")
    stub = video_detector_pb2_grpc.VideoDetectorStub(channel)
    
    try:
        response = stub.Detect(
            video_detector_pb2.VideoRequest(video_data=video_bytes),
            timeout=30.0
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
        print("Usage: python video_grpc_client_test.py <path_to_video>")
        sys.exit(1)
    
    test_video(sys.argv[1])