#!/bin/bash
set -euo pipefail

APP_DIR="${APP_DIR:-$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)}"
BRANCH="${DEPLOY_BRANCH:-master}"
REGISTRY="${REGISTRY:-}"
REGISTRY_USERNAME="${REGISTRY_USERNAME:-}"
REGISTRY_TOKEN="${REGISTRY_TOKEN:-}"

log() {
  printf '[deploy] %s\n' "$1"
}

remove_old_project_images() {
  local image_ref
  local current_image_id
  local image_ids

  for image_ref in "$@"; do
    current_image_id="$(docker image inspect "$image_ref" --format '{{.Id}}' 2>/dev/null || true)"

    image_ids="$(
      docker images "$image_ref" \
        --all \
        --quiet \
        --no-trunc \
        | sort -u
    )"

    if [[ -z "$image_ids" ]]; then
      continue
    fi

    while IFS= read -r image_id; do
      if [[ -z "$image_id" || "$image_id" == "$current_image_id" ]]; then
        continue
      fi

      docker image rm "$image_id" >/dev/null 2>&1 || true
    done <<< "$image_ids"
  done
}

cd "$APP_DIR"

log "Deploying Cogniflex from branch '$BRANCH' in '$APP_DIR'"

if ! git rev-parse --is-inside-work-tree >/dev/null 2>&1; then
  log "Error: '$APP_DIR' is not a git repository"
  exit 1
fi

CURRENT_BRANCH="$(git branch --show-current || true)"
if [[ -n "$CURRENT_BRANCH" && "$CURRENT_BRANCH" != "$BRANCH" ]]; then
  log "Checking out branch '$BRANCH' from '$CURRENT_BRANCH'"
  git checkout "$BRANCH"
fi

log "Fetching latest changes"
git fetch origin "$BRANCH"
git reset --hard "origin/$BRANCH"

if [[ -n "$REGISTRY" && -n "$REGISTRY_USERNAME" && -n "$REGISTRY_TOKEN" ]]; then
  log "Logging into container registry '$REGISTRY'"
  printf '%s' "$REGISTRY_TOKEN" | docker login "$REGISTRY" --username "$REGISTRY_USERNAME" --password-stdin
fi

if [[ -n "$REGISTRY" && -n "$REGISTRY_USERNAME" ]]; then
  IMAGE_PREFIX="$REGISTRY/$REGISTRY_USERNAME"

  log "Pulling custom images from '$IMAGE_PREFIX'"
  docker pull "$IMAGE_PREFIX/cogniflex-backend:latest"
  docker pull "$IMAGE_PREFIX/cogniflex-ml-service:latest"

  log "Tagging custom images for docker compose"
  docker tag "$IMAGE_PREFIX/cogniflex-backend:latest" cogniflex-backend:latest
  docker tag "$IMAGE_PREFIX/cogniflex-ml-service:latest" cogniflex-ml-service:latest
else
  log "No registry username provided, using local custom images"
fi

log "Pulling public service images"
docker compose pull db redis

log "Recreating services"
docker compose up -d --force-recreate --no-build

log "Removing old Cogniflex images"
if [[ -n "$REGISTRY" && -n "$REGISTRY_USERNAME" ]]; then
  remove_old_project_images \
    "cogniflex-backend" \
    "cogniflex-ml-service" \
    "$REGISTRY/$REGISTRY_USERNAME/cogniflex-backend" \
    "$REGISTRY/$REGISTRY_USERNAME/cogniflex-ml-service"
else
  remove_old_project_images \
    "cogniflex-backend" \
    "cogniflex-ml-service"
fi

log "Removing dangling images"
docker image prune -f

log "Deployment complete"
