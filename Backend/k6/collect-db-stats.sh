#!/usr/bin/env bash
# collect-db-stats.sh – Capture PostgreSQL query statistics before/after k6 tests.
#
# Usage:
#   ./k6/collect-db-stats.sh before   # Capture baseline
#   ./k6/collect-db-stats.sh after    # Capture post-test and compute deltas
#
# Prerequisites:
#   - PostgreSQL running on localhost:5432
#   - DB_USERNAME and DB_PASSWORD set (or defaults used)
set -euo pipefail

DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5432}"
DB_NAME="${DB_NAME:-RideBookDB}"
DB_USER="${DB_USERNAME:-root}"
RESULTS_DIR="$(dirname "$0")/results"
mkdir -p "$RESULTS_DIR"

PHASE="${1:-before}"

PGPASSWORD="${DB_PASSWORD:-@Tinh0804}" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -t -A -F',' <<'SQL' > "$RESULTS_DIR/db-stats-${PHASE}.csv"
SELECT
    schemaname,
    relname AS table_name,
    seq_scan,
    seq_tup_read,
    idx_scan,
    idx_tup_fetch,
    n_tup_ins,
    n_tup_upd,
    n_tup_del,
    n_live_tup
FROM pg_stat_user_tables
ORDER BY (seq_scan + COALESCE(idx_scan, 0)) DESC
LIMIT 30;
SQL

echo "[$PHASE] Saved to $RESULTS_DIR/db-stats-${PHASE}.csv"

if [ "$PHASE" = "after" ] && [ -f "$RESULTS_DIR/db-stats-before.csv" ]; then
    echo ""
    echo "═══════════════════════════════════════════════════"
    echo "  Database Query Delta (after - before)"
    echo "═══════════════════════════════════════════════════"

    paste -d',' "$RESULTS_DIR/db-stats-before.csv" "$RESULTS_DIR/db-stats-after.csv" | \
    awk -F',' '{
        if (NF >= 20) {
            table     = $2;
            seq_before = $3;  seq_after  = $13;
            idx_before = $5;  idx_after  = $15;
            total_before = seq_before + idx_before;
            total_after  = seq_after  + idx_after;
            delta = total_after - total_before;
            if (delta > 0)
                printf "  %-30s  seq_scan: +%-8d  idx_scan: +%-8d  total: +%d\n",
                    table,
                    seq_after - seq_before,
                    idx_after - idx_before,
                    delta;
        }
    }'

    echo ""
    echo "Tip: Run this script twice – once with cache ON, once with cache OFF."
    echo "     Compare the 'total' column to calculate DB load reduction %."
fi
