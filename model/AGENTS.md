# model/AGENTS.md

ML research workspace: data exploration, training, evaluation and export of models.
Nothing here is deployed. Read the root `AGENTS.md` first.

## Stack

- Python 3.12 (same as `inference/`), PyTorch, torchvision, Hugging Face `transformers`, scikit-learn,
  NumPy, pandas, OpenCV, MediaPipe, pytorchvideo, matplotlib; Jupyter notebooks for experiments
- Dependencies are listed in `requirements.txt`. Install them with **uv** into a local venv:

```bash
cd model
uv venv --python 3.12
uv pip install -r requirements.txt
uv run jupyter lab              # or select .venv as the kernel in your IDE
```

Never install into the system Python. When adding a dependency, append it to `requirements.txt`
with a lower-bound version (`package>=X.Y`).

## Layout

- `notebooks/` - experiments, training and optimization notebooks (`<topic>_<purpose>.ipynb`):
  text detection (RoBERTa), general and face image models (ResNet), production optimization
- `utils/` - research helpers grouped by modality: `image_utils/`, `text_utils/`, `video_utils/`
- `prod_utils/` - code meant to be ported to production (model selection, text preprocessing)
- `workflow_model/` - model validation script and its CI workflow definition
- `artifacts/` - local outputs (checkpoints, metrics, plots), never committed

New experiments get their own clearly named subpackage or notebook; do not create `test2.py`,
`new/`, `tmp/` or similar. Reusable logic moves out of notebooks into `utils/` modules.

## Rules

- **Never commit** datasets, weights, checkpoints, `*.npy`, `*.csv` dumps, Kaggle downloads or HF
  caches. Code must load them from a configurable path, never a hardcoded personal one
  (`/Users/<name>/...`). Document every dataset and model in `model/README.md`: source URL
  (or how to obtain it), license if known, and where to place it locally. If you do not know
  where a file came from, ask the user.
- **Type hints on every function** in `.py` files and on functions defined in notebooks, using
  modern syntax (`list[str]`, `X | None`).
- Notebooks: keep them runnable top to bottom, put paths and hyperparameters in one config cell,
  set random seeds, and clear heavy outputs (large images, long logs) before committing.
- Report results with the exact dataset split, metrics and seed so experiments are reproducible.
- `inference/` must never import from `model/`. To ship a model:
  1. export weights (`.safetensors` preferred over pickled `.pth`) to `artifacts/`,
  2. port the minimal typed inference/preprocessing code into `inference/app/detectors/`,
  3. document the weights in `inference/README.md` and the path in `inference/config.yaml`,
  4. keep preprocessing identical between training and inference and note it in the PR.
- The production runtime is CPU-only; mention CPU latency and model size when proposing a model.
- Training runs and large downloads are expensive - ask before starting them.

## Domain notes

Models estimate the probability that content is AI-generated: text (articles, posts, comments;
English and Russian), images (diffusion and other generators; separate general and face models)
and video (not implemented yet). Outputs are probabilities fused with static heuristics in the
backend, so calibrated confidence matters more than a hard label. The extension analyzes page
content in real time, so latency and robustness to short or noisy inputs matter.
