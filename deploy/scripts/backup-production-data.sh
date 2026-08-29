#!/usr/bin/env bash
set -euo pipefail

script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
deploy_dir="$(cd "$script_dir/.." && pwd)"
runtime_env_file="${RUNTIME_ENV_FILE:-/etc/ridebook/ridebook.env}"
state_dir="${RIDEBOOK_STATE_DIR:-/opt/ridebook/state}"
release_env_file="$state_dir/release.env"
backup_root="${RIDEBOOK_BACKUP_DIR:-/var/backups/ridebook}"
timestamp="$(date -u +%Y%m%dT%H%M%SZ)"

if [[ ! -f "$runtime_env_file" || ! -f "$release_env_file" ]]; then
    echo "Runtime or release env file is missing." >&2
    exit 1
fi

compose=(docker compose
    --env-file "$runtime_env_file"
    --env-file "$release_env_file"
    -f "$deploy_dir/compose.prod.yml")

postgres_container="$("${compose[@]}" ps -q postgres)"
redis_container="$("${compose[@]}" ps -q redis)"
if [[ -z "$postgres_container" || -z "$redis_container" ]]; then
    echo "PostgreSQL and Redis must both be running before backup." >&2
    exit 1
fi
target_db_name="$("${compose[@]}" exec -T postgres sh -c 'printf "%s" "$POSTGRES_DB"')"

umask 077
mkdir -p "$backup_root"

SOURCE_POSTGRES_CONTAINER="$postgres_container" \
SOURCE_REDIS_CONTAINER="$redis_container" \
SOURCE_DB_NAME="$target_db_name" \
    "$script_dir/export-current-data.sh" \
    --online-backup "$backup_root/backup-$timestamp"
