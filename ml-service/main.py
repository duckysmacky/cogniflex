import logging
import sys
from pathlib import Path

from app.proto import ensure_generated_proto


logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(message)s",
)

current_dir = Path(__file__).resolve().parent
project_root = current_dir.parent
generated_dir = current_dir / "generated"
proto_dir = project_root / "proto"

sys.path.insert(0, str(generated_dir))
sys.path.insert(0, str(current_dir))
sys.path.insert(0, str(project_root))

if not ensure_generated_proto(proto_dir, generated_dir):
    logging.error("Cannot start server without generated proto files")
    sys.exit(1)

from app.server import serve


if __name__ == "__main__":
    serve(current_dir, project_root)
