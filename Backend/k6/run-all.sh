#!/usr/bin/env bash
# run-all.sh – Orchestrate all k6 performance tests and generate a report.
#
# Usage:
#   cd Backend && ./k6/run-all.sh
#
# Prerequisites:
#   - k6 installed (brew install k6)
#   - Backend running on localhost:8080 (via IntelliJ)
#   - PostgreSQL + Redis running (docker compose up -d postgres redis)
#   - Test data seeded in DB (accounts, vehicle types, etc.)
#
# Environment variables (optional):
#   TEST_CUSTOMER_USERNAME  – default: 0912345678
#   TEST_CUSTOMER_PASSWORD  – default: 123456
#   TEST_DRIVER_USERNAME    – default: 0987654321
#   TEST_DRIVER_PASSWORD    – default: 123456
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
RESULTS_DIR="$SCRIPT_DIR/results"
REPORT_FILE="$RESULTS_DIR/performance-report.md"
mkdir -p "$RESULTS_DIR"

# ─── Colors ─────────────────────────────────────────────────────
RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; CYAN='\033[0;36m'; NC='\033[0m'

log()  { echo -e "${CYAN}[$(date +%H:%M:%S)]${NC} $*"; }
ok()   { echo -e "${GREEN}✓${NC} $*"; }
warn() { echo -e "${YELLOW}⚠${NC} $*"; }
fail() { echo -e "${RED}✗${NC} $*"; }

# ─── Pre-flight checks ─────────────────────────────────────────
log "Running pre-flight checks..."

if ! command -v k6 &>/dev/null; then
    fail "k6 not found. Install with: brew install k6"
    exit 1
fi
ok "k6 found: $(k6 version 2>&1 | head -1)"

HEALTH_URL="http://localhost:8080/RideBook/actuator/health"
if curl -sf "$HEALTH_URL" > /dev/null 2>&1; then
    ok "Backend is healthy"
else
    fail "Backend not reachable at $HEALTH_URL"
    echo "   Start the backend in IntelliJ first, then re-run this script."
    exit 1
fi

if redis-cli ping 2>/dev/null | grep -q PONG; then
    ok "Redis is running"
else
    warn "Redis not reachable – cache tests may fail"
fi

# ─── Helper: run a k6 scenario ─────────────────────────────────
run_scenario() {
    local name="$1"
    local script="$2"
    local json_out="$RESULTS_DIR/${name}.json"

    echo ""
    log "════════════════════════════════════════════════"
    log "  Running: $name"
    log "════════════════════════════════════════════════"

    k6 run "$script" \
        --summary-trend-stats="avg,min,med,max,p(90),p(95),p(99)" \
        --out "json=$json_out" \
        2>&1 | tee "$RESULTS_DIR/${name}.log"

    ok "$name complete → $json_out"
}

# ─── Phase 1: Collect baseline DB stats ─────────────────────────
log "Collecting baseline DB statistics..."
bash "$SCRIPT_DIR/collect-db-stats.sh" before 2>/dev/null || warn "DB stats collection skipped"

log "Collecting baseline Redis stats..."
redis-cli INFO stats > "$RESULTS_DIR/redis-stats-before.txt" 2>/dev/null || warn "Redis stats skipped"

# ─── Phase 2: Run scenarios ─────────────────────────────────────
run_scenario "read-heavy"     "$SCRIPT_DIR/scenarios/read-heavy.js"
run_scenario "mixed-workload" "$SCRIPT_DIR/scenarios/mixed-workload.js"
run_scenario "auth-flow"      "$SCRIPT_DIR/scenarios/auth-flow.js"

# ─── Phase 3: Collect post-test stats ───────────────────────────
log "Collecting post-test DB statistics..."
bash "$SCRIPT_DIR/collect-db-stats.sh" after 2>/dev/null || warn "DB stats collection skipped"

log "Collecting post-test Redis stats..."
redis-cli INFO stats > "$RESULTS_DIR/redis-stats-after.txt" 2>/dev/null || warn "Redis stats skipped"

# ─── Phase 4: Generate summary report ──────────────────────────
log "Generating performance report..."

TIMESTAMP=$(date -u +"%Y-%m-%dT%H:%M:%SZ")

cat > "$REPORT_FILE" <<EOF
# RideBook Backend – Performance Test Report

**Date**: $TIMESTAMP
**Hardware**: Apple MacBook Pro M1
**Backend**: Spring Boot (IntelliJ, native run)
**Database**: PostgreSQL 17 (Docker)
**Cache**: Redis 7.4 (Docker)
**Tool**: k6 $(k6 version 2>&1 | head -1)

---

## Test Scenarios Executed

| # | Scenario | Script | Duration |
|---|----------|--------|----------|
| 1 | Read-Heavy (cached endpoints) | \`read-heavy.js\` | ~3 min |
| 2 | Mixed Workload (70/20/10) | \`mixed-workload.js\` | ~3 min |
| 3 | Auth Flow | \`auth-flow.js\` | ~2 min |

## Results

> **Review the \`.log\` files in \`k6/results/\` for detailed k6 summaries.**

### Key files:
- \`results/read-heavy.log\` – Throughput + p95 latency
- \`results/mixed-workload.log\` – Realistic traffic simulation
- \`results/auth-flow.log\` – Auth endpoint performance
- \`results/db-stats-before.csv\` / \`db-stats-after.csv\` – DB query deltas
- \`results/redis-stats-before.txt\` / \`redis-stats-after.txt\` – Redis hit rates

## How to Measure 40% DB Load Reduction

1. Run this script with cache ON (default): \`./k6/run-all.sh\`
2. Restart backend with \`spring.cache.type=none\` in application.yaml
3. Run: \`CACHE_MODE=OFF k6 run k6/scenarios/cache-comparison.js --out json=k6/results/cache-off.json\`
4. Compare \`db-stats-after.csv\` between runs
5. Calculate: \`reduction = (queries_off - queries_on) / queries_off × 100%\`
EOF

ok "Report saved to $REPORT_FILE"

echo ""
log "════════════════════════════════════════════════"
log "  All tests complete!"
log "  Results: $RESULTS_DIR/"
log "════════════════════════════════════════════════"
echo ""
log "Next steps:"
echo "  1. Review .log files for throughput & latency numbers"
echo "  2. To measure cache impact, restart backend with spring.cache.type=none"
echo "     and run: k6 run k6/scenarios/cache-comparison.js"
echo "  3. Compare DB stats between cache ON and OFF runs"
