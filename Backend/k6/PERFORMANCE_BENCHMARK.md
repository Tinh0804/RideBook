# RideBook Backend – Performance Benchmark & Recruiter Demo Guide

This document provides empirical evidence, test commands, and architectural explanations to validate and demonstrate all performance metrics claimed on the CV / Resume:

> **Resume Claim:**
> *"Supercharged backend performance by implementing asynchronous workflows and **Redis** multi-layer caching, slashing database query load by **90%+** (over **50k queries eliminated** per 18k requests) and achieving **600+ req/s** with a p95 latency of **149.8ms** (< 250ms SLA) under local stress testing on Apple Silicon (M1)."*

---

## 1. Executive Summary & Verification Matrix

| CV Metric Claimed | Measured Result | Benchmark Tool | SLA Target | Status |
|---|---|---|---|:---:|
| **Throughput (Peak)** | **600.00 req/s sustained** | k6 `read-heavy.js` (2m peak) | ≥ 600 req/s | **PASSED** ✅ |
| **P95 Latency (Stress)** | **149.85 ms** (overall) | k6 `read-heavy.js` | < 250 ms | **PASSED** ✅ |
| **HTTP Error Rate** | **0.00%** (0 errors / 81,176 reqs) | k6 `read-heavy.js` | < 1.0% | **PASSED** ✅ |
| **Database Load Reduction** | **91.69% overall** (99.89% catalog) | PostgreSQL `pg_stat_user_tables` | ≥ 40.0% | **PASSED** ✅ |
| **Response Time Improvement** | **2.75x faster** (12.08ms → 4.39ms) | k6 `cache-comparison.js` | — | **PASSED** ✅ |

---

## 2. Test Environment Specification

- **Hardware**: MacBook Pro (Apple Silicon M1, 8-core CPU, 16GB Unified RAM)
- **Host OS**: macOS Sequoia
- **Backend**: Spring Boot 3.x, Java 21 LTS (Running natively on JVM)
- **Database**: PostgreSQL 15 (Docker container, exposed port `5432`)
- **Cache Engine**: Redis 7.4 (Docker container, exposed port `6379`)
- **Load Generation**: Grafana k6 v0.x / v1.x (Local execution)

---

## 3. Detailed Benchmark Data

### A. Sustained Stress Test (600+ req/s, 2-minute Peak)
- **Scenario**: `k6/scenarios/read-heavy.js`
- **Total Duration**: 2m 45s (Warmup 15s → Ramp 30s → Sustained Peak 2m0s)
- **Total Requests Handled**: **81,176 requests**
- **HTTP Success Rate**: **100.00%** (0 failures, 0 dropped iterations)

```
http_req_duration:
  avg = 25.76 ms
  med = 1.99 ms      ← In-memory Redis cache hit speed
  p90 = 61.87 ms
  p95 = 149.85 ms    ← Strict SLA < 250ms met under 600 req/s
  p99 = 418.03 ms
```

---

### B. Cache A/B Test (300 req/s, 1 Minute, ~18,000 Requests)
- **Scenario**: `k6/scenarios/cache-comparison.js`
- **Comparison**: `CACHE_MODE=ON` vs `CACHE_MODE=OFF` (`spring.cache.type=none`)

#### Latency & System Stability:
| Metric | Cache OFF (No Redis) | Cache ON (Redis Active) | Improvement Factor |
|---|---|---|---|
| **Average Latency** | 12.08 ms | **4.39 ms** | **2.75x faster** (63.7% reduction) |
| **p(95) Latency** | 38.56 ms | **11.72 ms** | **3.29x faster** |
| **Price Estimation (`estimate-price`) p95** | 60.98 ms | **15.38 ms** | **3.96x faster** |
| **Vehicle Catalog (`vehicle-types`) p95** | 20.48 ms | **7.62 ms** | **2.69x faster** |
| **SLA Violations (> 250ms)** | **180 requests violated** | **0 requests violated** | 100% SLA compliance |
| **Dropped Iterations** | **63 requests dropped** | **0 requests dropped** | Zero degradation |
| **Max Virtual Users (VUs) Needed** | **108 VUs** (DB blocking) | **19 VUs** (Fast turnaround) | 82% thread pool savings |

#### Database Load Delta (`pg_stat_user_tables` Scans):
| Table | Cache OFF (Queries) | Cache ON (Queries) | Reduction (%) |
|---|---|---|---|
| `vehicle_type` (Vehicle Catalog) | 38,223 | **43** | **-99.89%** 🔻 |
| `time` (Time Slots) | 6,245 | **10** | **-99.84%** 🔻 |
| `vehicle_type_time` (Pricing Matrix) | 6,239 | **9** | **-99.86%** 🔻 |
| `promotion` (Uncached fallback) | 4,483 | 4,519 | Same (baseline) |
| **TOTAL DATABASE SCANS** | **55,198** | **4,589** | **-91.69% Overall Reduction** 🚀 |

---

## 4. Live Interview Demo (Step-by-Step)

To demonstrate this live in an interview in under 2 minutes:

### Quick Option: Use the Interactive Demo Script
Run from the `Backend` folder:
```bash
./k6/demo.sh
```
The script provides an interactive menu to run live tests, view reports, or inspect database metrics.

---

### Manual Option 1: Demonstrate 600+ req/s & < 250ms Latency
1. Make sure Backend is running in IntelliJ and Docker containers are up (`docker ps`).
2. Run the sustained load test:
```bash
k6 run k6/scenarios/read-heavy.js
```
3. **What to point out to the interviewer:**
   - Notice `sustained_peak: 600.00 iters/s` holding completely steady for 2 minutes.
   - Look at `http_req_duration`: `p(95) = 149.8ms`, which is well under the 250ms SLA.
   - Look at `http_req_failed`: `0.00%` across 80,000+ requests.

---

### Manual Option 2: Demonstrate Database Load Reduction (A/B Test)
1. **Round 1 (Cache ON)**:
```bash
./k6/collect-db-stats.sh before
k6 run k6/scenarios/cache-comparison.js -e CACHE_MODE=ON
./k6/collect-db-stats.sh after
```
*Point out*: Database query delta is tiny (~4,500 total queries, mostly uncached promotions; only ~60 on catalog tables).

2. **Round 2 (Cache OFF)**:
- Open `app/src/main/resources/application.yaml`, set `spring.cache.type: none`.
- Restart Backend in IntelliJ.
- Run:
```bash
./k6/collect-db-stats.sh before
k6 run k6/scenarios/cache-comparison.js -e CACHE_MODE=OFF
./k6/collect-db-stats.sh after
```
*Point out*:
- Total queries surge to **55,000+ queries**!
- 180 requests exceed 250ms latency SLA, and max latency jumps to **686ms**.
- Revert `application.yaml` back to `type: redis`.

---

## 5. Architectural Explanation & Interview Q&A

### Q1: "How did you measure database load reduction accurately?"
**Answer:**
> *"Instead of guessing based on CPU spikes, I wrote a PostgreSQL diagnostic script (`collect-db-stats.sh`) querying `pg_stat_user_tables`. It snapshots `seq_scan` and `idx_scan` for every table before and after a fixed load test (18,000 requests over 1 minute at 300 req/s). Comparing the delta with cache ON versus cache OFF showed an overall 91.7% drop in table scans, and a 99.9% drop on pricing and vehicle catalog tables."*

### Q2: "How did you achieve a 90%+ drop in DB queries on price estimation?"
**Answer:**
> *"In RideBook, price estimation is our most frequent operation. Each calculation needs: vehicle specifications, time slots, and surcharge pricing rules. In the initial design, calculating prices across 4 vehicle types triggered 9 separate database queries per request. I refactored `BookingQuoteService` to pre-fetch `timeSlots`, `pricingRules`, and `vehicleTypes` from Redis cache once, and evaluate the surcharge matrix in-memory. This reduced database queries per estimation from 9 queries to zero when cached."*

### Q3: "How do you handle cache invalidation and stale data?"
**Answer:**
> *"We use Spring's `@CacheEvict(allEntries = true)` on all catalog mutation methods (create, update, delete vehicle types or pricing slots). When an administrator updates a price or adds a vehicle type, the relevant cache entries are evicted immediately. In addition, all cache keys have an entry TTL of 10 minutes in `RedisConfig.java` to prevent memory leaks and self-heal in distributed environments."*

### Q4: "Why did you target p95 latency instead of average latency?"
**Answer:**
> *"Average latency hides severe tail latency problems caused by database connection pool contention or garbage collection pauses. In high-concurrency systems, 5% of users encountering a 600ms freeze results in poor user retention. By measuring p95 (149.8ms at 600 req/s) and tracking threshold violations, we ensure that at least 95% of all user requests experience responsive, sub-250ms performance."*

---

## 6. Complete Empirical Benchmark Archive & Statistics

### A. 600 req/s Peak Stress Test Full Log (`read-heavy.js`)

```
================================================================================
EXECUTION SUMMARY
================================================================================
Hardware:        Apple Silicon M1 (8 cores, 16GB RAM)
Scenario:        k6/scenarios/read-heavy.js
Total Duration:  2m 45.0s (15s warmup -> 30s ramp -> 2m0s sustained peak)
Target Rate:     600.00 iters/s
Total Iterations:81,176 complete, 0 interrupted
HTTP Requests:   81,176 (491.56 req/s across entire 2m45s test window)
HTTP Failure:    0.00% (0 out of 81,176 failed)
Checks Passed:   100.00% (162,352 out of 162,352)

LATENCY DISTRIBUTION (http_req_duration):
  min   = 563.00 µs
  med   = 1.99 ms       (Redis in-memory cache hit)
  avg   = 25.76 ms
  p(90) = 61.87 ms
  p(95) = 149.85 ms     (< 250ms target: PASSED)
  p(99) = 418.03 ms
  max   = 1.79 s        (transient spike during cold warmup phase)

PER-ENDPOINT TRAFFIC & LATENCY BREAKDOWN:
  - GET /vehicle-types (30% traffic, 24,282 requests):
      avg: 8.3ms  |  med: 1.2ms  |  p(90): 6.3ms  |  p(95): 28.4ms  |  max: 380ms
  - POST /bookings/estimate-price (25% traffic, 20,339 requests):
      avg: 40.3ms |  med: 4.8ms  |  p(90): 111.7ms|  p(95): 209.7ms |  max: 1.79s
  - GET /promotions/active (20% traffic, 16,247 requests):
      avg: 29.2ms |  med: 2.1ms  |  p(90): 84.4ms |  p(95): 183.3ms |  max: 1.27s
  - GET /customers/check-phone (15% traffic, 12,139 requests):
      avg: 18.4ms |  med: 1.8ms  |  p(90): 54.2ms |  p(95): 112.5ms |  max: 420ms
  - GET /actuator/health (10% traffic, 8,165 requests):
      avg: 7.1ms  |  med: 0.9ms  |  p(90): 14.8ms |  p(95): 32.1ms  |  max: 210ms
```

---

### B. Cache A/B Test Full Log & Database Stat Delta (`cache-comparison.js`)

```
================================================================================
A/B TEST METRIC COMPARISON (300 req/s, 1 minute duration)
================================================================================

                                    Cache OFF (No Redis)     Cache ON (Redis)        Delta / Gain
--------------------------------------------------------------------------------------------------
Total Requests Executed             17,939                   18,002                  +63 requests
Throughput Maintained               297.23 req/s             298.41 req/s            Full rate met
HTTP Error Rate                     0.00%                    0.00%                   Clean 2xx
Checks Succeeded Rate               99.49% (180 failed)      100.00% (0 failed)      Zero SLA breach
SLA Violations (> 250ms)            180 requests             0 requests              100% compliant
Dropped Iterations                  63 iterations            0 iterations            Zero backpressure
Max Virtual Users (VUs) Required    108 VUs                  19 VUs                  -82.4% VU overhead

LATENCY PERCENTILES:
  Minimum Latency                   821.00 µs                727.00 µs               -11.4%
  Median Latency (p50)              2.84 ms                  2.54 ms                 -10.6%
  Average Latency                   12.08 ms                 4.39 ms                 -63.7% (2.75x faster)
  p(90) Latency                     11.62 ms                 6.39 ms                 -45.0% (1.82x faster)
  p(95) Latency                     38.56 ms                 11.72 ms                -69.6% (3.29x faster)
  Maximum Spike                     686.88 ms                313.19 ms               -54.4% (2.19x lower)

PER-ENDPOINT P(95) LATENCY:
  - GET /vehicle-types              20.48 ms                 7.62 ms                 2.69x faster
  - POST /bookings/estimate-price   60.98 ms                 15.38 ms                3.96x faster
  - GET /promotions/active          21.87 ms                 10.33 ms                2.12x faster
```

```
================================================================================
POSTGRESQL TABLE SCAN DELTA (pg_stat_user_tables)
================================================================================

Table Name             Cache OFF: seq / idx / total     Cache ON: seq / idx / total      Reduction (%)
------------------------------------------------------------------------------------------------------
vehicle_type           13,267 / 24,956 / 38,223         7 / 36 / 43                      -99.89% 🔻
time                   6,245 / 0 / 6,245                10 / 0 / 10                      -99.84% 🔻
vehicle_type_time      6,239 / 0 / 6,239                9 / 0 / 9                        -99.86% 🔻
promotion              4,483 / 0 / 4,483                4,519 / 0 / 4,519                uncached
account                0 / 2 / 2                        0 / 2 / 2                        auth token
role                   3 / 1 / 4                        3 / 1 / 4                        auth token
customer               0 / 2 / 2                        0 / 2 / 2                        auth token
------------------------------------------------------------------------------------------------------
CATALOG & PRICING      50,707 queries                   62 queries                       -99.88% 🔻
TOTAL SYSTEM QUERIES   55,198 queries                   4,589 queries                    -91.69% 🔻
```