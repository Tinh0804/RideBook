/**
 * auth-flow.js – Test authentication endpoints under load.
 *
 * Measures:
 *   - Login throughput and latency
 *   - Token introspection (Redis blacklist check)
 *   - Token refresh
 *
 * Usage:
 *   k6 run k6/scenarios/auth-flow.js
 */
import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';
import { BASE_URL, jsonHeaders } from '../helpers.js';

// ─── Custom Metrics ────────────────────────────────────────────
const errorRate     = new Rate('custom_error_rate');
const loginDur      = new Trend('auth_login_duration', true);
const introspectDur = new Trend('auth_introspect_duration', true);
const refreshDur    = new Trend('auth_refresh_duration', true);

// ─── Options ───────────────────────────────────────────────────
export const options = {
    scenarios: {
        auth_load: {
            executor: 'ramping-arrival-rate',
            startRate: 10,
            timeUnit: '1s',
            stages: [
                { target: 100, duration: '15s' },
                { target: 200, duration: '15s' },
                { target: 200, duration: '1m' },
                { target: 0,   duration: '10s' },
            ],
            preAllocatedVUs: 30,
            maxVUs: 150,
        },
    },
    thresholds: {
        http_req_duration: ['p(95)<500'],   // Auth is heavier – 500ms threshold
        http_req_failed:   ['rate<0.05'],
    },
};

const TEST_USERNAME = __ENV.TEST_CUSTOMER_USERNAME || 'customer';
const TEST_PASSWORD = __ENV.TEST_CUSTOMER_PASSWORD || '123456';
const TEST_ROLE     = 'CUSTOMER';

// ─── Default: auth workflow ────────────────────────────────────
export default function () {
    const roll = Math.random();

    if (roll < 0.50) {
        // 50 % – Login (heaviest: DB lookup + bcrypt + JWT generation)
        const res = http.post(`${BASE_URL}/auth/login`, JSON.stringify({
            userName: TEST_USERNAME,
            passWord: TEST_PASSWORD,
            roleName: TEST_ROLE,
        }), jsonHeaders());

        loginDur.add(res.timings.duration);
        const ok = check(res, {
            'login – status 200':  (r) => r.status === 200,
            'login – has token':   (r) => {
                try { return JSON.parse(r.body).result.token !== undefined; }
                catch { return false; }
            },
        });
        errorRate.add(!ok);

        // Use the token for subsequent introspect/refresh
        if (ok) {
            try {
                const body = JSON.parse(res.body).result;

                // Introspect
                const intrRes = http.post(`${BASE_URL}/auth/introspect`, JSON.stringify({
                    token: body.token,
                }), jsonHeaders());
                introspectDur.add(intrRes.timings.duration);
                const intrOk = check(intrRes, {
                    'introspect – status 200': (r) => r.status === 200,
                });
                errorRate.add(!intrOk);

            } catch (e) {
                // Parse error – skip
            }
        }

    } else {
        // 50 % – Introspect with a fresh login token
        const loginRes = http.post(`${BASE_URL}/auth/login`, JSON.stringify({
            userName: TEST_USERNAME,
            passWord: TEST_PASSWORD,
            roleName: TEST_ROLE,
        }), jsonHeaders());

        if (loginRes.status === 200) {
            try {
                const body = JSON.parse(loginRes.body).result;

                // Refresh token
                const refreshRes = http.post(`${BASE_URL}/auth/refresh-token`, JSON.stringify({
                    token: body.refreshToken,
                }), jsonHeaders());
                refreshDur.add(refreshRes.timings.duration);
                const refOk = check(refreshRes, {
                    'refresh – status 200': (r) => r.status === 200,
                });
                errorRate.add(!refOk);

            } catch (e) {
                // Parse error
            }
        }
    }
}

export function teardown() {
    console.log('Auth-flow test complete.');
    console.log('Key metrics: auth_login_duration, auth_introspect_duration, auth_refresh_duration');
}
