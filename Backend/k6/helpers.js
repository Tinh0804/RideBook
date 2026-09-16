import http from 'k6/http';
import { check, sleep } from 'k6';

// ─── Configuration ─────────────────────────────────────────────
export const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080/RideBook';

// Test credentials matching local DB
const TEST_CUSTOMER_USERNAME = __ENV.TEST_CUSTOMER_USERNAME || 'customer';
const TEST_CUSTOMER_PASSWORD = __ENV.TEST_CUSTOMER_PASSWORD || '123456';
const TEST_CUSTOMER_ROLE     = 'CUSTOMER';

const TEST_DRIVER_USERNAME = __ENV.TEST_DRIVER_USERNAME || 'driver';
const TEST_DRIVER_PASSWORD = __ENV.TEST_DRIVER_PASSWORD || '123456';
const TEST_DRIVER_ROLE     = 'DRIVER';

// ─── Auth helpers ──────────────────────────────────────────────

/**
 * Login and return { token, refreshToken, profileId }.
 * Call this in setup() so we only authenticate once per test run.
 */
export function login(username, password, roleName) {
    const res = http.post(
        `${BASE_URL}/auth/login`,
        JSON.stringify({
            userName: username,
            passWord: password,
            roleName: roleName,
        }),
        { headers: { 'Content-Type': 'application/json' } },
    );

    const ok = check(res, {
        'login status 200': (r) => r.status === 200,
        'login has token':  (r) => {
            try { return JSON.parse(r.body).result.token !== undefined; }
            catch { return false; }
        },
    });

    if (!ok) {
        console.error(`Login failed for ${username}: ${res.status} – ${res.body}`);
        return null;
    }

    const body = JSON.parse(res.body).result;
    return {
        token:        body.token,
        refreshToken: body.refreshToken,
        profileId:    body.account ? body.account.profileId : null,
    };
}

export function loginCustomer() {
    return login(TEST_CUSTOMER_USERNAME, TEST_CUSTOMER_PASSWORD, TEST_CUSTOMER_ROLE);
}

export function loginDriver() {
    return login(TEST_DRIVER_USERNAME, TEST_DRIVER_PASSWORD, TEST_DRIVER_ROLE);
}

// ─── Request helpers ───────────────────────────────────────────

export function authHeaders(token) {
    return {
        headers: {
            Authorization: `Bearer ${token}`,
            'Content-Type': 'application/json',
        },
    };
}

export function jsonHeaders() {
    return {
        headers: { 'Content-Type': 'application/json' },
    };
}

/**
 * Standard check: 2xx and body is valid JSON with status 200.
 */
export function checkOk(res, label) {
    return check(res, {
        [`${label} – status 2xx`]:   (r) => r.status >= 200 && r.status < 300,
        [`${label} – latency < 250ms`]: (r) => r.timings.duration < 250,
    });
}

// ─── Coordinate helpers (Ho Chi Minh City area) ────────────────

export function randomHCMCPoint() {
    // Bounding box: roughly HCMC center
    const lat = 10.75 + Math.random() * 0.1;   // 10.75 – 10.85
    const lng = 106.62 + Math.random() * 0.1;   // 106.62 – 106.72
    return { lat, lng };
}
