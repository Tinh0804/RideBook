#!/usr/bin/env bash
set -euo pipefail

if [[ $# -ne 2 || "$2" != "--confirm-empty-target" ]]; then
    echo "Usage: $0 /secure/path/to/source-backup --confirm-empty-target" >&2
    exit 2
fi

backup_dir="$(cd "$1" && pwd)"
script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
deploy_dir="$(cd "$script_dir/.." && pwd)"
runtime_env_file="${RUNTIME_ENV_FILE:-/etc/ridebook/ridebook.env}"
release_env_file="${RELEASE_ENV_FILE:-/opt/ridebook/state/release.env}"

for required_file in postgres.dump redis.rdb checksums.sha256 redis-key-count.txt postgres-table-count.txt; do
    if [[ ! -f "$backup_dir/$required_file" ]]; then
        echo "Missing backup file: $backup_dir/$required_file" >&2
        exit 1
    fi
done

if [[ ! -f "$runtime_env_file" || ! -f "$release_env_file" ]]; then
    echo "Runtime or release env file is missing." >&2
    exit 1
fi

if command -v sha256sum >/dev/null 2>&1; then
    (cd "$backup_dir" && sha256sum --check checksums.sha256)
else
    (cd "$backup_dir" && shasum -a 256 --check checksums.sha256)
fi

compose=(docker compose
    --env-file "$runtime_env_file"
    --env-file "$release_env_file"
    -f "$deploy_dir/compose.prod.yml")

wait_for_health() {
    local service="$1"
    local container_id
    container_id="$("${compose[@]}" ps -q "$service")"
    if [[ -z "$container_id" ]]; then
        echo "No container found for service '$service'." >&2
        exit 1
    fi

    for _ in {1..60}; do
        status="$(docker inspect --format '{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}' "$container_id")"
        if [[ "$status" == "healthy" ]]; then
            return
        fi
        if [[ "$status" == "unhealthy" || "$status" == "exited" ]]; then
            echo "Service '$service' entered state '$status'." >&2
            exit 1
        fi
        sleep 2
    done
    echo "Service '$service' did not become healthy." >&2
    exit 1
}

echo "Starting the empty target PostgreSQL service..."
"${compose[@]}" up -d postgres
wait_for_health postgres

target_table_count="$("${compose[@]}" exec -T postgres sh -c \
    'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -Atc "SELECT count(*) FROM information_schema.tables WHERE table_schema = '\''public'\'' AND table_type = '\''BASE TABLE'\''"')"
if [[ "$target_table_count" != "0" ]]; then
    echo "Target PostgreSQL already has $target_table_count public tables; refusing to overwrite it." >&2
    exit 1
fi

echo "Restoring PostgreSQL..."
"${compose[@]}" exec -T postgres sh -c \
    'exec pg_restore -U "$POSTGRES_USER" -d "$POSTGRES_DB" --exit-on-error --no-owner --no-acl' \
    < "$backup_dir/postgres.dump"

restored_table_count="$("${compose[@]}" exec -T postgres sh -c \
    'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -Atc "SELECT count(*) FROM information_schema.tables WHERE table_schema = '\''public'\'' AND table_type = '\''BASE TABLE'\''"')"
source_table_count="$(tr -d '[:space:]' < "$backup_dir/postgres-table-count.txt")"
if [[ "$restored_table_count" != "$source_table_count" ]]; then
    echo "PostgreSQL table count mismatch: source=$source_table_count target=$restored_table_count" >&2
    exit 1
fi

echo "Restoring Redis RDB into its new named volume..."
"${compose[@]}" stop redis >/dev/null 2>&1 || true
"${compose[@]}" run --rm --no-deps \
    -v "$backup_dir:/restore:ro" \
    --entrypoint sh redis \
    -c 'cp /restore/redis.rdb /data/dump.rdb && chown redis:redis /data/dump.rdb'
"${compose[@]}" up -d redis
wait_for_health redis

source_key_count="$(tr -d '[:space:]' < "$backup_dir/redis-key-count.txt")"
restored_key_count="$("${compose[@]}" exec -T redis redis-cli --raw DBSIZE | tr -d '[:space:]')"
if [[ "$source_key_count" != "0" && "$restored_key_count" == "0" ]]; then
    echo "Redis restored zero keys from a non-empty source snapshot." >&2
    exit 1
fi

echo "Initial restore completed."
echo "PostgreSQL tables: $restored_table_count"
echo "Redis keys: source=$source_key_count target=$restored_key_count (expired TTL keys may reduce the target count)"
echo "Backend and Nginx remain stopped until application smoke checks are ready."
