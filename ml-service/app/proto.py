import logging
import os
import subprocess
import sys
from pathlib import Path


def ensure_generated_proto(proto_dir: Path, generated_dir: Path) -> bool:
    proto_file = proto_dir / "ml_analyzer.proto"
    if not proto_file.exists():
        logging.error("Proto file not found: %s", proto_file)
        return False

    generated_dir.mkdir(parents=True, exist_ok=True)

    pb2_file = generated_dir / "ml_analyzer_pb2.py"
    pb2_grpc_file = generated_dir / "ml_analyzer_pb2_grpc.py"

    if pb2_file.exists() and pb2_grpc_file.exists():
        proto_mtime = os.path.getmtime(proto_file)
        pb2_mtime = os.path.getmtime(pb2_file)
        if pb2_mtime > proto_mtime:
            logging.info("Proto files are up to date, skipping generation")
            return True

    logging.info("Generating gRPC files from proto...")
    try:
        subprocess.run(
            [
                sys.executable,
                "-m",
                "grpc_tools.protoc",
                f"-I{proto_dir}",
                f"--python_out={generated_dir}",
                f"--grpc_python_out={generated_dir}",
                str(proto_file),
            ],
            check=True,
        )
        logging.info("gRPC files generated successfully")
        return True
    except subprocess.CalledProcessError as e:
        logging.error("Failed to generate gRPC files: %s", e)
        return False