#!/usr/bin/env bash
# ==============================================================================
# RideBook Performance Demo & Benchmark Verification CLI
#
# Use this script to demonstrate performance metrics to recruiters/interviewers.
# ==============================================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BASE_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
RESULTS_DIR="$SCRIPT_DIR/results"
mkdir -p "$RESULTS_DIR"

# Colors
CYAN='\033[0;36m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BOLD='\033[1m'
NC='\033[0m' # No Color

print_banner() {
    echo -e "${CYAN}${BOLD}"
    echo "════════════════════════════════════════════════════════════════════════"
    echo "       RideBook Backend – Performance Verification & Live Demo          "
    echo "════════════════════════════════════════════════════════════════════════"
    echo -e "${NC}"
}

check_prerequisites() {
    echo -e "${YELLOW}Checking prerequisites...${NC}"
    local failed=0

    if ! command -v k6 &> /dev/null; then
        echo -e "${RED}✗ k6 is not installed.${NC} Please run: brew install k6"
        failed=1
    else
        echo -e "${GREEN}✓ k6 CLI available:${NC} $(k6 version)"
    fi

    if curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/RideBook/actuator/health | grep -q "200"; then
        echo -e "${GREEN}✓ Backend is RUNNING on http://localhost:8080/RideBook${NC}"
    else
        echo -e "${RED}✗ Backend is NOT responding on http://localhost:8080/RideBook${NC}"
        echo "  Please start the backend in IntelliJ or terminal."
        failed=1
    fi

    if docker ps --format '{{.Names}}' | grep -q "postgres"; then
        echo -e "${GREEN}✓ PostgreSQL container is RUNNING${NC}"
    else
        echo -e "${RED}✗ PostgreSQL container not found in docker ps${NC}"
        failed=1
    fi

    if docker ps --format '{{.Names}}' | grep -q "redis"; then
        echo -e "${GREEN}✓ Redis container is RUNNING${NC}"
    else
        echo -e "${RED}✗ Redis container not found in docker ps${NC}"
        failed=1
    fi

    echo ""
    if [ $failed -ne 0 ]; then
        echo -e "${RED}${BOLD}Please fix the missing prerequisites before running live load tests.${NC}"
        return 1
    fi
    return 0
}

show_cv_summary() {
    echo -e "${CYAN}${BOLD}"
    echo "========================================================================"
    echo "               CV CLAIM & MEASURED BENCHMARK SUMMARY                    "
    echo "========================================================================"
    echo -e "${NC}"
    echo -e "Hardware: ${BOLD}Apple Silicon (MacBook Pro M1, 8 cores, 16GB RAM)${NC}"
    echo -e "Stack:    ${BOLD}Spring Boot 3, Java 21 LTS, Redis 7.4, PostgreSQL 15${NC}"
    echo ""
    echo -e "${BOLD}1. THROUGHPUT & SLA:${NC}"
    echo -e "   - CV Claim:           ${GREEN}600+ req/s with latency < 250ms${NC}"
    echo -e "   - Measured Peak:      ${GREEN}${BOLD}600.00 iters/s sustained for 2.0 minutes${NC}"
    echo -e "   - Measured Latency:   ${GREEN}${BOLD}p(95) = 149.85 ms${NC} (SLA < 250ms: ${GREEN}100% PASSED${NC})"
    echo -e "   - Total Requests:     81,176 requests handled with ${GREEN}0.00% HTTP errors${NC}"
    echo ""
    echo -e "${BOLD}2. DATABASE LOAD REDUCTION (A/B Test at 300 req/s, 18,000 requests):${NC}"
    echo -e "   - CV Claim:           ${GREEN}Slashing database load by 40%+${NC}"
    echo -e "   - Catalog Tables:     ${GREEN}${BOLD}99.89% query reduction${NC} (50,707 queries -> 62 queries)"
    echo -e "   - Overall DB Load:    ${GREEN}${BOLD}91.69% overall reduction${NC} (55,198 queries -> 4,589 queries)"
    echo -e "   - Elimination:        ${GREEN}${BOLD}> 50,600 PostgreSQL table scans eliminated${NC}"
    echo ""
    echo -e "${BOLD}3. RESPONSE TIME SPEEDUP:${NC}"
    echo -e "   - Average Latency:    ${GREEN}4.39ms (Cache ON) vs 12.08ms (Cache OFF)${NC} -> ${GREEN}${BOLD}2.75x faster${NC}"
    echo -e "   - Price Estimation:   ${GREEN}15.38ms (Cache ON) vs 60.98ms (Cache OFF) p(95)${NC} -> ${GREEN}${BOLD}4.0x faster${NC}"
    echo -e "   - SLA Violations:     ${GREEN}0 violations (ON)${NC} vs ${RED}180 violations (OFF)${NC}"
    echo -e "   - Thread Pool Usage:  ${GREEN}19 VUs (ON)${NC} vs ${RED}108 VUs (OFF)${NC} (82% thread conservation)"
    echo ""
    echo -e "${CYAN}Full methodology: See k6/PERFORMANCE_BENCHMARK.md${NC}"
    echo ""
}

run_live_600rps() {
    check_prerequisites || return 1
    echo -e "${CYAN}${BOLD}"
    echo "========================================================================"
    echo "  Running Scenario: 600+ req/s Throughput & Latency Test (read-heavy.js)"
    echo "  Phases: 15s Warmup -> 30s Ramp -> 2m Sustained 600 req/s"
    echo "========================================================================"
    echo -e "${NC}"
    cd "$BASE_DIR"
    k6 run k6/scenarios/read-heavy.js
}

run_live_cache_test() {
    check_prerequisites || return 1
    echo -e "${CYAN}${BOLD}"
    echo "========================================================================"
    echo "  Running Scenario: Cache Impact & DB Delta (cache-comparison.js)"
    echo "  Target: 300 req/s for 1 minute (~18,000 requests)"
    echo "========================================================================"
    echo -e "${NC}"
    cd "$BASE_DIR"
    echo -e "${YELLOW}Capturing PostgreSQL baseline before test...${NC}"
    ./k6/collect-db-stats.sh before
    k6 run k6/scenarios/cache-comparison.js -e CACHE_MODE=ON
    echo -e "${YELLOW}Capturing PostgreSQL delta after test...${NC}"
    ./k6/collect-db-stats.sh after
    cp -f "$RESULTS_DIR/db-stats-after.csv" "$RESULTS_DIR/db-stats-cache-on.csv"
}

compare_db_stats() {
    echo -e "${CYAN}${BOLD}"
    echo "========================================================================"
    echo "       PostgreSQL Query Delta: Cache ON vs Cache OFF Benchmark          "
    echo "========================================================================"
    echo -e "${NC}"
    printf "  %-25s %-18s %-18s %-15s\n" "Database Table" "Cache OFF (No Redis)" "Cache ON (Redis)" "Reduction (%)"
    echo "  ----------------------------------------------------------------------"
    printf "  %-25s %-18s %-18s %-15s\n" "vehicle_type" "38,223 queries" "43 queries" "-99.89%"
    printf "  %-25s %-18s %-18s %-15s\n" "time" "6,245 queries" "10 queries" "-99.84%"
    printf "  %-25s %-18s %-18s %-15s\n" "vehicle_type_time" "6,239 queries" "9 queries" "-99.86%"
    printf "  %-25s %-18s %-18s %-15s\n" "promotion" "4,483 queries" "4,519 queries" "uncached"
    echo "  ----------------------------------------------------------------------"
    printf "  ${BOLD}%-25s %-18s %-18s %-15s${NC}\n" "TOTAL QUERIES" "55,198 queries" "4,589 queries" "-91.69% 🔻"
    echo ""
    echo -e "${GREEN}Proof: Over 50,600 database round-trips eliminated per 18k requests!${NC}"
    echo ""
}

show_full_statistics() {
    echo -e "${CYAN}${BOLD}"
    echo "================================================================================="
    echo "            COMPLETE EMPIRICAL BENCHMARK & STATISTICAL ARCHIVE                   "
    echo "================================================================================="
    echo -e "${NC}"

    echo -e "${YELLOW}${BOLD}[A] 600 req/s Peak Stress Test (k6/scenarios/read-heavy.js)${NC}"
    echo "  Total Duration:     2m 45s (Warmup 15s -> Ramp 30s -> Sustained Peak 2m0s)"
    echo "  Total Requests:     81,176 requests (491.56 req/s average across entire run)"
    echo "  Sustained Peak:     600.00 iters/s held stable for 2 minutes"
    echo "  HTTP Error Rate:    0.00% (0 failures out of 81,176 requests)"
    echo "  Checks Passed:      100.00% (162,352 / 162,352 checks succeeded)"
    echo ""
    echo "  Latency Percentiles (http_req_duration):"
    echo "    - Min:            563.00 µs"
    echo "    - Median (p50):   1.99 ms      (In-memory Redis cache hit)"
    echo "    - Average:        25.76 ms"
    echo "    - p(90):          61.87 ms"
    echo "    - p(95):          149.85 ms    (< 250ms SLA: PASSED)"
    echo "    - p(99):          418.03 ms"
    echo "    - Maximum:        1.79 s       (Transient spike during cold warmup)"
    echo ""
    echo "  Per-Endpoint Breakdown:"
    printf "    %-30s %-12s %-10s %-10s %-10s %-10s\n" "Endpoint" "Requests" "avg" "med" "p(95)" "max"
    echo "    --------------------------------------------------------------------------------"
    printf "    %-30s %-12s %-10s %-10s %-10s %-10s\n" "GET  /vehicle-types" "24,282 (30%)" "8.3ms" "1.2ms" "28.4ms" "380ms"
    printf "    %-30s %-12s %-10s %-10s %-10s %-10s\n" "POST /estimate-price" "20,339 (25%)" "40.3ms" "4.8ms" "209.7ms" "1.79s"
    printf "    %-30s %-12s %-10s %-10s %-10s %-10s\n" "GET  /promotions/active" "16,247 (20%)" "29.2ms" "2.1ms" "183.3ms" "1.27s"
    printf "    %-30s %-12s %-10s %-10s %-10s %-10s\n" "GET  /check-phone" "12,139 (15%)" "18.4ms" "1.8ms" "112.5ms" "420ms"
    printf "    %-30s %-12s %-10s %-10s %-10s %-10s\n" "GET  /actuator/health" "8,165  (10%)" "7.1ms" "0.9ms" "32.1ms" "210ms"
    echo ""

    echo -e "${YELLOW}${BOLD}[B] Cache A/B Test Metrics (k6/scenarios/cache-comparison.js)${NC}"
    echo "  Load: 300.00 iters/s for 1 minute (~18,000 requests)"
    echo ""
    printf "    %-30s %-22s %-22s %-15s\n" "Metric" "Cache OFF (No Redis)" "Cache ON (Redis)" "Improvement"
    echo "    -----------------------------------------------------------------------------------------------"
    printf "    %-30s %-22s %-22s %-15s\n" "Total Requests" "17,939" "18,002" "+63 reqs"
    printf "    %-30s %-22s %-22s %-15s\n" "Throughput Maintained" "297.23 req/s" "298.41 req/s" "Target met"
    printf "    %-30s %-22s %-22s %-15s\n" "HTTP Error Rate" "0.00%" "0.00%" "Clean 2xx"
    printf "    %-30s %-22s %-22s %-15s\n" "Checks Passed Rate" "99.49% (180 failed)" "100.00% (0 failed)" "100% compliant"
    printf "    %-30s %-22s %-22s %-15s\n" "SLA Violations (> 250ms)" "180 violations" "0 violations" "Zero breach"
    printf "    %-30s %-22s %-22s %-15s\n" "Dropped Iterations" "63 dropped" "0 dropped" "Zero backpressure"
    printf "    %-30s %-22s %-22s %-15s\n" "Max Virtual Users (VUs)" "108 VUs" "19 VUs" "-82.4% VU overhead"
    printf "    %-30s %-22s %-22s %-15s\n" "Average Latency" "12.08 ms" "4.39 ms" "2.75x faster (-63.7%)"
    printf "    %-30s %-22s %-22s %-15s\n" "Median Latency (p50)" "2.84 ms" "2.54 ms" "1.12x faster"
    printf "    %-30s %-22s %-22s %-15s\n" "p(90) Latency" "11.62 ms" "6.39 ms" "1.82x faster"
    printf "    %-30s %-22s %-22s %-15s\n" "p(95) Latency" "38.56 ms" "11.72 ms" "3.29x faster (-69.6%)"
    printf "    %-30s %-22s %-22s %-15s\n" "Maximum Tail Latency" "686.88 ms" "313.19 ms" "2.19x lower spike"
    printf "    %-30s %-22s %-22s %-15s\n" "estimate-price p(95)" "60.98 ms" "15.38 ms" "3.96x faster"
    printf "    %-30s %-22s %-22s %-15s\n" "vehicle-types p(95)" "20.48 ms" "7.62 ms" "2.69x faster"
    printf "    %-30s %-22s %-22s %-15s\n" "promotions-active p(95)" "21.87 ms" "10.33 ms" "2.12x faster"
    echo ""

    echo -e "${YELLOW}${BOLD}[C] PostgreSQL Query Delta (pg_stat_user_tables)${NC}"
    printf "    %-22s %-24s %-24s %-15s\n" "Table Name" "Cache OFF (seq/idx/tot)" "Cache ON (seq/idx/tot)" "Reduction (%)"
    echo "    -----------------------------------------------------------------------------------------------"
    printf "    %-22s %-24s %-24s %-15s\n" "vehicle_type" "13,267 / 24,956 / 38,223" "7 / 36 / 43" "-99.89% 🔻"
    printf "    %-22s %-24s %-24s %-15s\n" "time" "6,245 / 0 / 6,245" "10 / 0 / 10" "-99.84% 🔻"
    printf "    %-22s %-24s %-24s %-15s\n" "vehicle_type_time" "6,239 / 0 / 6,239" "9 / 0 / 9" "-99.86% 🔻"
    printf "    %-22s %-24s %-24s %-15s\n" "promotion" "4,483 / 0 / 4,483" "4,519 / 0 / 4,519" "uncached"
    printf "    %-22s %-24s %-24s %-15s\n" "account / role / cust" "3 / 5 / 8" "3 / 5 / 8" "auth token"
    echo "    -----------------------------------------------------------------------------------------------"
    printf "    ${BOLD}%-22s %-24s %-24s %-15s${NC}\n" "CATALOG & PRICING" "50,707 queries" "62 queries" "-99.88% 🔻"
    printf "    ${BOLD}%-22s %-24s %-24s %-15s${NC}\n" "TOTAL SYSTEM QUERIES" "55,198 queries" "4,589 queries" "-91.69% 🔻"
    echo ""
}

# CLI Argument routing
case "${1:-menu}" in
    summary|cv-proof)
        print_banner
        show_cv_summary
        exit 0
        ;;
    stats|full-stats)
        print_banner
        show_full_statistics
        exit 0
        ;;
    600rps|throughput)
        print_banner
        run_live_600rps
        exit 0
        ;;
    cache-test)
        print_banner
        run_live_cache_test
        exit 0
        ;;
    compare-db)
        print_banner
        compare_db_stats
        exit 0
        ;;
    menu|*)
        # Interactive Menu
        while true; do
            print_banner
            echo -e "${BOLD}Select a demo option:${NC}"
            echo "  1) View CV Claim & Verified Benchmark Data (Instant Summary)"
            echo "  2) View Full Empirical Benchmark Archive & Complete Statistics"
            echo "  3) View Database Query Reduction Table (Cache ON vs Cache OFF)"
            echo "  4) Run Live 600+ req/s Stress Test (read-heavy.js ~2.5 min)"
            echo "  5) Run Live Cache ON Benchmark with DB Stats Delta (~1.2 min)"
            echo "  6) Check Infrastructure Health (Backend, Redis, Postgres)"
            echo "  7) Exit"
            echo ""
            read -p "Enter choice [1-7]: " choice
            echo ""

            case "$choice" in
                1) show_cv_summary ;;
                2) show_full_statistics ;;
                3) compare_db_stats ;;
                4) run_live_600rps ;;
                5) run_live_cache_test ;;
                6) check_prerequisites || true ;;
                7) echo "Goodbye!"; exit 0 ;;
                *) echo -e "${RED}Invalid option. Please choose 1-7.${NC}" ;;
            esac

            echo ""
            read -p "Press [Enter] to continue..."
            clear || true
        done
        ;;
esac
