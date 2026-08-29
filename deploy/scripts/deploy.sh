#!/usr/bin/env bash
set -euo pipefail

image_ref="${1:-}"
if [[ ! "$image_ref" =~ ^[a-zA-Z0-9._:/-]+@sha256:[a-f0-9]{64}$ ]]; then
    echo "Usage: $0 registry/repository@sha256:<64-hex-digest>" >&2
    exit 2
fi

script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
deploy_dir="$(cd "$script_dir/.." && pwd)"
runtime_env_file="${RUNTIME_ENV_FILE:-/etc/ridebook/ridebook.env}"
state_dir="${RIDEBOOK_STATE_DIR:-/opt/ridebook/state}"
current_link="${RIDEBOOK_CURRENT_LINK:-/opt/ridebook/releases/current}"
release_env_file="$state_dir/release.env"
previous_image_file="$state_dir/previous-image"

if [[ ! -f "$runtime_env_file" ]]; then
    echo "Runtime environment file not found: $runtime_env_file" >&2
    exit 1
fi

umask 077
mkdir -p "$state_dir"
exec 9>"$state_dir/deploy.lock"
if ! flock -n 9; then
    echo "Another RideBook deployment is already running." >&2
    exit 1
fi

current_image=""
if [[ -f "$release_env_file" ]]; then
    current_image="$(sed -n 's/^BACKEND_IMAGE=//p' "$release_env_file" | head -n 1)"
fi
if [[ -n "$current_image" && "$current_image" != "$image_ref" ]]; then
    printf '%s\n' "$current_image" > "$previous_image_file"
fi

write_release_image() {
    local target_image="$1"
    local temporary_file="$release_env_file.tmp"
    printf 'BACKEND_IMAGE=%s\n' "$target_image" > "$temporary_file"
    mv "$temporary_file" "$release_env_file"
}

compose=(docker compose
    --env-file "$runtime_env_file"
    --env-file "$release_env_file"
    -f "$deploy_dir/compose.prod.yml")

wait_for_health() {
    local service="$1"
    local attempts="$2"
    local container_id
    container_id="$("${compose[@]}" ps -q "$service")"
    if [[ -z "$container_id" ]]; then
        return 1
    fi

    for ((attempt = 1; attempt <= attempts; attempt++)); do
        status="$(docker inspect --format '{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}' "$container_id")"
        if [[ "$status" == "healthy" ]]; then
            return 0
        fi
        if [[ "$status" == "unhealthy" || "$status" == "exited" ]]; then
            return 1
        fi
        sleep 2
    done
    return 1
}

rollback_backend() {
    if [[ -z "$current_image" || "$current_image" == "$image_ref" ]]; then
        echo "No previous Backend image is available for automatic rollback." >&2
        return 1
    fi

    echo "Backend health check failed; rolling back to the previous image."
    write_release_image "$current_image"
    "${compose[@]}" pull backend
    "${compose[@]}" up -d --no-build backend nginx
    wait_for_health backend 45 && wait_for_health nginx 20
}

write_release_image "$image_ref"
"${compose[@]}" config --quiet
"${compose[@]}" pull backend
"${compose[@]}" up -d --no-build

if ! wait_for_health backend 60 || ! wait_for_health nginx 30; then
    rollback_backend
    exit 1
fi

ln -sfn "$deploy_dir" "$current_link"
echo "RideBook deployment is healthy: $image_ref"
