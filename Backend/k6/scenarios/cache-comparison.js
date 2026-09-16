/**
 * cache-comparison.js – A/B test to measure Redis cache impact.
 *
 * Run TWICE:
 *   Round 1 (cache ON):  k6 run k6/scenarios/cache-comparison.js --out json=k6/results/cache-on.json
 *   Round 2 (cache OFF): Restart backend with spring.cache.type=none, then:
 *                         k6 run k6/scenarios/cache-comparison.js --out json=k6/results/cache-off.json
 *
 * Then compare:
 *   - avg / p95 / p99 response times
 *   - PostgreSQL pg_stat_user_tables.seq_scan + idx_scan deltas
 *   - Redis INFO stats keyspace_hits / (keyspace_hits + keyspace_misses)
 *
 * This scenario ONLY targets endpoints backed by @Cacheable to isolate cache impact.
 */
import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';
import { BASE_URL, loginCustomer, authHeaders, jsonHeaders, checkOk } from '../helpers.js';

// ─── Custom Metrics ────────────────────────────────────────────
const errorRate        = new Rate('custom_error_rate');
const vehicleTypesDur  = new Trend('cached_vehicle_types_duration', true);
const estimatePriceDur = new Trend('cached_estimate_price_duration', true);
const promotionsDur    = new Trend('cached_promotions_duration', true);

// ─── Options ───────────────────────────────────────────────────
export const options = {
    scenarios: {
        // Moderate load – enough to show cache impact without overwhelming
        cache_comparison: {
            executor: 'constant-arrival-rate',
            rate: 300,
            timeUnit: '1s',
            duration: '1m',
            preAllocatedVUs: 50,
            maxVUs: 150,
        },
    },
    thresholds: {
        // Relaxed thresholds – cache OFF will be slower
        http_req_failed: ['rate<0.05'],
    },
};

// ─── Setup ─────────────────────────────────────────────────────
export function setup() {
    const customer = loginCustomer();
    const cacheMode = __ENV.CACHE_MODE || 'UNKNOWN';
    console.log(`\n════════════════════════════════════════`);
    console.log(`  Cache Comparison Test – Mode: ${cacheMode}`);
    console.log(`════════════════════════════════════════\n`);
    return { customer, cacheMode };
}

// ─── Default: only @Cacheable endpoints ────────────────────────
export default function (data) {
    const token = data.customer ? data.customer.token : null;
    const opts  = token ? authHeaders(token) : jsonHeaders();

    const roll = Math.random();

    if (roll < 0.40) {
        // 40 % – GET /vehicle-types  → @Cacheable("vehicleTypes")
        const res = http.get(`${BASE_URL}/vehicle-types`, opts);
        vehicleTypesDur.add(res.timings.duration);
        const ok = checkOk(res, 'vehicle-types');
        errorRate.add(!ok);

    } else if (roll < 0.75) {
        // 35 % – POST /bookings/estimate-price → uses @Cacheable vehicleTypes + pricing internally
        const res = http.post(`${BASE_URL}/bookings/estimate-price`, JSON.stringify({
            pickupLat:  10.78 + Math.random() * 0.05,
            pickupLng:  106.65 + Math.random() * 0.05,
            dropoffLat: 10.80 + Math.random() * 0.05,
            dropoffLng: 106.68 + Math.random() * 0.05,
        }), { headers: { 'Content-Type': 'application/json' } });
        estimatePriceDur.add(res.timings.duration);
        const ok = checkOk(res, 'estimate-price');
        errorRate.add(!ok);

    } else {
        // 25 % – GET /promotions/active
        const res = http.get(`${BASE_URL}/promotions/active`, opts);
        promotionsDur.add(res.timings.duration);
        const ok = checkOk(res, 'promotions-active');
        errorRate.add(!ok);
    }
}

// ─── Teardown ──────────────────────────────────────────────────
export function teardown(data) {
    console.log(`\n════════════════════════════════════════`);
    console.log(`  Cache mode: ${data.cacheMode}`);
    console.log(`  Review the summary above.`);
    console.log(`  Compare vehicle_types_duration, estimate_price_duration,`);
    console.log(`  and promotions_duration between cache ON and OFF runs.`);
    console.log(`════════════════════════════════════════\n`);
}
