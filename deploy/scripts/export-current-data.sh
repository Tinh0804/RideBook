#!/usr/bin/env bash
set -euo pipefail

snapshot_mode="${1:-}"
case "$snapshot_mode" in
    --confirm-writes-stopped)
        ;;
    --online-backup)
        echo "Creating an online operational backup; PostgreSQL and Redis are individually consistent."
        ;;
    *)
        echo "Usage: $0 --confirm-writes-stopped|--online-backup [output-directory]" >&2
        echo "Use --confirm-writes-stopped for the final migration snapshot." >&2
        exit 2
        ;;
esac

source_postgres_container="${SOURCE_POSTGRES_CONTAINER:-postgres}"
source_redis_container="${SOURCE_REDIS_CONTAINER:-redis}"
source_db_name="${SOURCE_DB_NAME:-RideBookDB}"
timestamp="$(date -u +%Y%m%dT%H%M%SZ)"
backup_dir="${2:-$(pwd)/backups/source-${timestamp}}"

if [[ ! "$source_db_name" =~ ^[a-zA-Z0-9_]+$ ]]; then
    echo "SOURCE_DB_NAME contains unsupported characters." >&2
    exit 2
fi

umask 077
mkdir -p "$backup_dir"

require_running_container() {
    local container="$1"
    local running
    running="$(docker inspect --format '{{.State.Running}}' "$container" 2>/dev/null || true)"
    if [[ "$running" != "true" ]]; then
        echo "Container '$container' is not running." >&2
        exit 1
    fi
}

checksum_files() {
    if command -v sha256sum >/dev/null 2>&1; then
        (cd "$backup_dir" && sha256sum postgres.dump redis.rdb > checksums.sha256)
    else
        (cd "$backup_dir" && shasum -a 256 postgres.dump redis.rdb > checksums.sha256)
    fi
}

require_running_container "$source_postgres_container"
require_running_container "$source_redis_container"

echo "Creating PostgreSQL logical backup from '$source_postgres_container'..."
docker exec "$source_postgres_container" sh -c \
    'exec pg_dump -U "$POSTGRES_USER" -d "$1" -Fc --no-owner --no-acl' sh "$source_db_name" \
    > "$backup_dir/postgres.dump"

docker exec "$source_postgres_container" postgres --version \
    > "$backup_dir/postgres-version.txt"
docker exec "$source_postgres_container" sh -c \
    'psql -U "$POSTGRES_USER" -d "$1" -Atc "SELECT count(*) FROM information_schema.tables WHERE table_schema = '\''public'\'' AND table_type = '\''BASE TABLE'\''"' sh "$source_db_name" \
    > "$backup_dir/postgres-table-count.txt"

echo "Creating Redis RDB snapshot from '$source_redis_container'..."
bgsave_result="$(docker exec "$source_redis_container" redis-cli --raw BGSAVE 2>&1 || true)"
if [[ "$bgsave_result" != "Background saving started" && "$bgsave_result" != *"already in progress"* ]]; then
    echo "Redis BGSAVE failed: $bgsave_result" >&2
    exit 1
fi

for _ in {1..60}; do
    persistence="$(docker exec "$source_redis_container" redis-cli INFO persistence | tr -d '\r')"
    if grep -q '^rdb_bgsave_in_progress:0$' <<<"$persistence"; then
        if ! grep -q '^rdb_last_bgsave_status:ok$' <<<"$persistence"; then
            echo "Redis reported an unsuccessful RDB snapshot." >&2
            exit 1
        fi
        break
    fi
    sleep 1
done

if grep -q '^rdb_bgsave_in_progress:1$' <<<"${persistence:-}"; then
    echo "Redis BGSAVE did not finish within 60 seconds." >&2
    exit 1
fi

docker cp "$source_redis_container:/data/dump.rdb" "$backup_dir/redis.rdb"
docker exec "$source_redis_container" redis-server --version \
    > "$backup_dir/redis-version.txt"
docker exec "$source_redis_container" redis-cli --raw DBSIZE \
    > "$backup_dir/redis-key-count.txt"

checksum_files

cat > "$backup_dir/manifest.txt" <<EOF
created_at=${timestamp}
snapshot_mode=${snapshot_mode}
postgres_container=${source_postgres_container}
postgres_database=${source_db_name}
redis_container=${source_redis_container}
postgres_dump=postgres.dump
redis_dump=redis.rdb
EOF

echo "Backup created at: $backup_dir"
echo "Keep this directory private and transfer it only through an encrypted channel."
