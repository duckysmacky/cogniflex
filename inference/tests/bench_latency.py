"""Closed-loop load benchmark against a running inference server.

Usage: uv run tests/bench_latency.py [--image path.jpg] [--duration 10] [--out result.json]

Each scenario starts N client threads per request kind; every client sends
requests back to back for --duration seconds. Prints p50/p95 latency per kind
and total throughput, optionally dumps everything as JSON.
"""

import argparse
import io
import json
import os
import statistics
import sys
import threading
import time
from collections.abc import Callable

import grpc
from common import load_env_file

sys.path.insert(0, os.path.join(os.path.dirname(__file__), "..", "generated"))

from cogniflex.inference import analyzer_pb2, analyzer_pb2_grpc

SHORT_TEXT = "The quick brown fox jumps over the lazy dog while the sun slowly sets behind the hills."
LONG_TEXT = " ".join([SHORT_TEXT] * 60)  # well over 512 tokens

SCENARIOS: list[tuple[str, dict[str, int]]] = [
    ("short x1", {"short": 1}),
    ("short x4", {"short": 4}),
    ("long x1", {"long": 1}),
    ("long x4", {"long": 4}),
    ("photo x1", {"photo": 1}),
    ("photo x4", {"photo": 4}),
    ("short x8", {"short": 8}),
    ("photo x8", {"photo": 8}),
    ("mixed 2s+2l+2p", {"short": 2, "long": 2, "photo": 2}),
    ("ext-like 4s+4p", {"short": 4, "photo": 4}),
]


def make_image_bytes() -> bytes:
    from PIL import Image

    image = Image.frombytes("RGB", (1024, 768), os.urandom(1024 * 768 * 3))
    buffer = io.BytesIO()
    image.save(buffer, format="JPEG")
    return buffer.getvalue()


def percentile(ordered: list[float], fraction: float) -> float:
    return ordered[min(len(ordered) - 1, int(len(ordered) * fraction))]


def run_scenario(calls: dict[str, Callable[[], None]], mix: dict[str, int], duration: float) -> dict:
    latencies: dict[str, list[float]] = {kind: [] for kind in mix}
    errors: dict[str, int] = {kind: 0 for kind in mix}
    lock = threading.Lock()
    deadline = time.perf_counter() + duration

    def client(kind: str) -> None:
        while time.perf_counter() < deadline:
            start = time.perf_counter()
            try:
                calls[kind]()
            except grpc.RpcError:
                with lock:
                    errors[kind] += 1
                continue
            elapsed = (time.perf_counter() - start) * 1000
            with lock:
                latencies[kind].append(elapsed)

    threads = [threading.Thread(target=client, args=(kind,)) for kind, n in mix.items() for _ in range(n)]
    started = time.perf_counter()
    for thread in threads:
        thread.start()
    for thread in threads:
        thread.join()
    wall = time.perf_counter() - started

    result: dict = {"throughput_rps": sum(len(v) for v in latencies.values()) / wall, "kinds": {}}
    for kind, samples in latencies.items():
        ordered = sorted(samples)
        result["kinds"][kind] = {
            "n": len(ordered),
            "errors": errors[kind],
            "p50_ms": statistics.median(ordered) if ordered else None,
            "p95_ms": percentile(ordered, 0.95) if ordered else None,
        }
    return result


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--image")
    parser.add_argument("--duration", type=float, default=10.0)
    parser.add_argument("--out")
    args = parser.parse_args()

    load_env_file(os.path.join(os.path.dirname(__file__), "..", "..", ".env"))
    target = f"{os.getenv('ML_GRPC_HOST', 'localhost')}:{os.getenv('ML_GRPC_PORT', '50051')}"
    stub = analyzer_pb2_grpc.AnalyzerStub(grpc.insecure_channel(target))

    if args.image:
        with open(args.image, "rb") as f:
            image_bytes = f.read()
    else:
        image_bytes = make_image_bytes()

    calls: dict[str, Callable[[], None]] = {
        "short": lambda: stub.AnalyzeText(analyzer_pb2.TextRequest(text=SHORT_TEXT), timeout=120),
        "long": lambda: stub.AnalyzeText(analyzer_pb2.TextRequest(text=LONG_TEXT), timeout=120),
        "photo": lambda: stub.AnalyzePhoto(analyzer_pb2.PhotoRequest(image_data=image_bytes), timeout=120),
    }

    for call in calls.values():  # warm up
        for _ in range(3):
            call()

    results: dict[str, dict] = {}
    for name, mix in SCENARIOS:
        result = run_scenario(calls, mix, args.duration)
        results[name] = result
        parts = [
            f"{kind} p50={s['p50_ms']:.0f} p95={s['p95_ms']:.0f} n={s['n']}" + (f" err={s['errors']}" if s["errors"] else "")
            for kind, s in result["kinds"].items()
            if s["p50_ms"] is not None
        ]
        print(f"{name:<16} {result['throughput_rps']:6.2f} rps | " + " | ".join(parts), flush=True)

    if args.out:
        with open(args.out, "w", encoding="utf-8") as f:
            json.dump(results, f, indent=2)


if __name__ == "__main__":
    main()
