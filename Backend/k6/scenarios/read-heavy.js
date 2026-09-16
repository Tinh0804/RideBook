/**
 * read-heavy.js – Stress test cached read endpoints.
 *
 * Target metrics:
 *   - Throughput ≥ 600 req/s
 *   - p95 latency < 250 ms
 *   - Error rate < 1 %
 *
 * Usage:
 *   k6 run k6/scenarios/read-heavy.js
 *   k6 run k6/scenarios/read-heavy.js --out json=k6/results/read-heavy.json
 */
import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';
import { BASE_URL, loginCustomer, authHeaders, checkOk, randomHCMCPoint } from '../helpers.js';

// ─── Custom Metrics ────────────────────────────────────────────
const httpErrorRate     = new Rate('http_error_rate');       // HTTP 4xx/5xx only
const latencyViolation  = new Rate('latency_violation_rate'); // p95 > 250ms breaches
const vehicleTypeDur    = new Trend('vehicle_types_duration', true);
const estimateDur       = new Trend('estimate_price_duration', true);
const promotionsDur     = new Trend('promotions_duration', true);

// ─── Options ───────────────────────────────────────────────────
export const options = {
    scenarios: {
        // Phase 1: Warm up caches
        warmup: {
            executor: 'shared-iterations',
            vus: 5,
            iterations: 20,
            startTime: '0s',
            maxDuration: '15s',
        },
        // Phase 2: Ramp to target throughput
        ramp_up: {
            executor: 'ramping-arrival-rate',
            startRate: 50,
            timeUnit: '1s',
            stages: [
                { target: 300, duration: '15s' },
                { target: 600, duration: '15s' },
            ],
            preAllocatedVUs: 50,
            maxVUs: 200,
            startTime: '15s',
        },
        // Phase 3: Sustained peak
        sustained_peak: {
            executor: 'constant-arrival-rate',
            rate: 600,
            timeUnit: '1s',
            duration: '2m',
            preAllocatedVUs: 100,
            maxVUs: 300,
            startTime: '45s',
        },
    },
    thresholds: {
        http_req_duration:      ['p(95)<250'],   // p95 < 250 ms (primary CV claim)
        http_req_failed:        ['rate<0.01'],   // < 1 % HTTP errors
        http_error_rate:        ['rate<0.01'],   // < 1 % HTTP errors (custom)
        latency_violation_rate: ['rate<0.05'],   // < 5 % individual reqs > 250ms allowed
    },
};

// ─── Setup: authenticate once ──────────────────────────────────
export function setup() {
    const customer = loginCustomer();
    if (!customer) {
        console.warn('Customer login failed – some endpoints will return 401');
    }

    // Hit cached endpoints once to warm Spring @Cacheable
    if (customer) {
        http.get(`${BASE_URL}/vehicle-types`, authHeaders(customer.token));
        http.get(`${BASE_URL}/promotions/active`, authHeaders(customer.token));
    }

    // Public endpoints warm-up
    const pickup  = randomHCMCPoint();
    const dropoff = randomHCMCPoint();
    http.post(`${BASE_URL}/bookings/estimate-price`, JSON.stringify({
        pickupLat:  pickup.lat,
        pickupLng:  pickup.lng,
        dropoffLat: dropoff.lat,
        dropoffLng: dropoff.lng,
    }), { headers: { 'Content-Type': 'application/json' } });

    return { customer };
}

// ─── Default function: mixed read requests ─────────────────────
export default function (data) {
    const token = data.customer ? data.customer.token : null;
    const opts  = token ? authHeaders(token) : { headers: { 'Content-Type': 'application/json' } };

    const roll = Math.random();

    if (roll < 0.30) {
        // 30 % – GET /vehicle-types  (Redis @Cacheable)
        const res = http.get(`${BASE_URL}/vehicle-types`, opts);
        vehicleTypeDur.add(res.timings.duration);
        httpErrorRate.add(res.status >= 400);
        latencyViolation.add(res.timings.duration > 250);
        check(res, { 'vehicle-types – status 2xx': (r) => r.status < 400 });

    } else if (roll < 0.55) {
        // 25 % – POST /bookings/estimate-price  (public, uses cached vehicleTypes + pricing)
        const pickup  = randomHCMCPoint();
        const dropoff = randomHCMCPoint();
        const res = http.post(`${BASE_URL}/bookings/estimate-price`, JSON.stringify({
            pickupLat:  pickup.lat,
            pickupLng:  pickup.lng,
            dropoffLat: dropoff.lat,
            dropoffLng: dropoff.lng,
        }), { headers: { 'Content-Type': 'application/json' } });
        estimateDur.add(res.timings.duration);
        httpErrorRate.add(res.status >= 400);
        latencyViolation.add(res.timings.duration > 250);
        check(res, { 'estimate-price – status 2xx': (r) => r.status < 400 });

    } else if (roll < 0.75) {
        // 20 % – GET /promotions/active  (Redis-backed)
        const res = http.get(`${BASE_URL}/promotions/active`, opts);
        promotionsDur.add(res.timings.duration);
        httpErrorRate.add(res.status >= 400);
        latencyViolation.add(res.timings.duration > 250);
        check(res, { 'promotions-active – status 2xx': (r) => r.status < 400 });

    } else if (roll < 0.90) {
        // 15 % – GET /auth/check-phone (lightweight DB check)
        const res = http.get(`${BASE_URL}/auth/check-phone?phone=0912345678`, opts);
        httpErrorRate.add(res.status >= 400);
        latencyViolation.add(res.timings.duration > 250);
        check(res, { 'check-phone – status 2xx': (r) => r.status < 400 });

    } else {
        // 10 % – GET /actuator/health (baseline, no DB)
        const res = http.get(`${BASE_URL}/actuator/health`);
        httpErrorRate.add(res.status >= 400);
        check(res, { 'health – status 2xx': (r) => r.status < 400 });
    }
}

// ─── Teardown ──────────────────────────────────────────────────
export function teardown(data) {
    console.log('Read-heavy test complete.');
    console.log('Review k6 summary above for throughput, p95, and error rate.');
}
