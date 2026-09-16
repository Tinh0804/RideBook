/**
 * mixed-workload.js – Simulates realistic production traffic.
 *
 * Traffic distribution:
 *   70 % reads  (cached data)
 *   20 % writes (estimate price, booking queries)
 *   10 % heavy  (paginated lists, admin summary)
 *
 * Usage:
 *   k6 run k6/scenarios/mixed-workload.js
 */
import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { Rate, Trend, Counter } from 'k6/metrics';
import {
    BASE_URL, loginCustomer, loginDriver,
    authHeaders, jsonHeaders, checkOk, randomHCMCPoint,
} from '../helpers.js';

// ─── Custom Metrics ────────────────────────────────────────────
const errorRate  = new Rate('custom_error_rate');
const readCount  = new Counter('read_requests');
const writeCount = new Counter('write_requests');
const heavyCount = new Counter('heavy_requests');

// ─── Options ───────────────────────────────────────────────────
export const options = {
    scenarios: {
        mixed_traffic: {
            executor: 'ramping-arrival-rate',
            startRate: 50,
            timeUnit: '1s',
            stages: [
                { target: 200, duration: '20s' },
                { target: 400, duration: '20s' },
                { target: 600, duration: '20s' },
                { target: 600, duration: '90s' },   // sustained peak
                { target: 100, duration: '15s' },
                { target: 0,   duration: '10s' },
            ],
            preAllocatedVUs: 80,
            maxVUs: 400,
        },
    },
    thresholds: {
        http_req_duration: ['p(95)<250', 'p(99)<500'],
        http_req_failed:   ['rate<0.01'],
        custom_error_rate: ['rate<0.01'],
    },
};

// ─── Setup ─────────────────────────────────────────────────────
export function setup() {
    const customer = loginCustomer();
    const driver   = loginDriver();

    if (!customer) console.warn('⚠ Customer login failed');
    if (!driver)   console.warn('⚠ Driver login failed');

    return { customer, driver };
}

// ─── Default: weighted random endpoint selection ───────────────
export default function (data) {
    const customerToken = data.customer ? data.customer.token : null;
    const driverToken   = data.driver   ? data.driver.token   : null;
    const customerOpts  = customerToken ? authHeaders(customerToken) : jsonHeaders();
    const driverOpts    = driverToken   ? authHeaders(driverToken)   : jsonHeaders();

    const roll = Math.random();

    // ── 70 % READS (cached) ────────────────────────────────────
    if (roll < 0.70) {
        readCount.add(1);
        const subRoll = Math.random();

        if (subRoll < 0.30) {
            // GET /vehicle-types – @Cacheable
            const res = http.get(`${BASE_URL}/vehicle-types`, customerOpts);
            const ok = checkOk(res, 'read:vehicle-types');
            errorRate.add(!ok);

        } else if (subRoll < 0.55) {
            // POST /bookings/estimate-price – public, hits cached pricing
            const pickup  = randomHCMCPoint();
            const dropoff = randomHCMCPoint();
            const res = http.post(`${BASE_URL}/bookings/estimate-price`, JSON.stringify({
                pickupLat: pickup.lat, pickupLng: pickup.lng,
                dropoffLat: dropoff.lat, dropoffLng: dropoff.lng,
            }), jsonHeaders());
            const ok = checkOk(res, 'read:estimate-price');
            errorRate.add(!ok);

        } else if (subRoll < 0.75) {
            // GET /promotions/active
            const res = http.get(`${BASE_URL}/promotions/active`, customerOpts);
            const ok = checkOk(res, 'read:promotions');
            errorRate.add(!ok);

        } else if (subRoll < 0.90) {
            // GET /actuator/health – baseline
            const res = http.get(`${BASE_URL}/actuator/health`);
            const ok = checkOk(res, 'read:health');
            errorRate.add(!ok);

        } else {
            // GET /auth/check-phone
            const res = http.get(`${BASE_URL}/auth/check-phone?phone=0900000001`);
            const ok = checkOk(res, 'read:check-phone');
            errorRate.add(!ok);
        }

    // ── 20 % WRITES / auth-dependent reads ─────────────────────
    } else if (roll < 0.90) {
        writeCount.add(1);
        const subRoll = Math.random();

        if (subRoll < 0.40) {
            // GET /customers/my-info – auth, DB lookup
            if (customerToken) {
                const res = http.get(`${BASE_URL}/customers/my-info`, customerOpts);
                const ok = checkOk(res, 'write:my-info');
                errorRate.add(!ok);
            }

        } else if (subRoll < 0.70) {
            // GET /notifications – auth, notification DB query
            if (customerToken) {
                const res = http.get(`${BASE_URL}/notifications`, customerOpts);
                const ok = checkOk(res, 'write:notifications');
                errorRate.add(!ok);
            }

        } else {
            // GET /drivers/my-info – driver profile
            if (driverToken) {
                const res = http.get(`${BASE_URL}/drivers/my-info`, driverOpts);
                const ok = checkOk(res, 'write:driver-info');
                errorRate.add(!ok);
            }
        }

    // ── 10 % HEAVY (paginated / aggregate) ─────────────────────
    } else {
        heavyCount.add(1);
        const subRoll = Math.random();

        if (subRoll < 0.40) {
            // GET /bookings/available – driver GEO search
            if (driverToken) {
                const res = http.get(`${BASE_URL}/bookings/available`, driverOpts);
                const ok = checkOk(res, 'heavy:available-rides');
                errorRate.add(!ok);
            }

        } else if (subRoll < 0.70) {
            // POST /auth/introspect – token validation + Redis check
            if (customerToken) {
                const res = http.post(`${BASE_URL}/auth/introspect`, JSON.stringify({
                    token: customerToken,
                }), jsonHeaders());
                const ok = checkOk(res, 'heavy:introspect');
                errorRate.add(!ok);
            }

        } else {
            // GET /loyalty/config – loyalty config read
            if (customerToken) {
                const res = http.get(`${BASE_URL}/loyalty/config`, customerOpts);
                const ok = checkOk(res, 'heavy:loyalty-config');
                errorRate.add(!ok);
            }
        }
    }
}

export function teardown(data) {
    console.log('Mixed-workload test complete.');
}
