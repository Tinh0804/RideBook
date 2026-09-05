#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$repo_root"

fail() {
    echo "FAIL: $1" >&2
    exit 1
}

assert_contains() {
    local file="$1"
    local pattern="$2"
    grep -Fq -- "$pattern" "$file" || fail "$file must contain: $pattern"
}

assert_not_contains() {
    local file="$1"
    local pattern="$2"
    if grep -Fq -- "$pattern" "$file"; then
        fail "$file must not contain: $pattern"
    fi
}

assert_contains ".github/workflows/cd.yml" "Legacy CD is disabled"
assert_not_contains ".github/workflows/cd.yml" "secrets.ENV_PROD"
assert_not_contains ".github/workflows/cd.yml" "appleboy/"
assert_contains ".github/workflows/ci.yml" "bash deploy/tests/deployment-contract.sh"
assert_contains ".github/workflows/release-backend.yml" "bash deploy/tests/deployment-contract.sh"
assert_contains ".github/workflows/release-backend.yml" "id-token: write"
assert_contains ".github/workflows/ci.yml" "branches: ['main','master','develop','test']"
assert_not_contains ".github/workflows/ci.yml" "docker/login-action"
assert_contains ".github/workflows/ci.yml" "push: false"

assert_contains "deploy/compose.prod.yml" '${RUNTIME_ENV_FILE:-/etc/ridebook/ridebook.env}'
assert_not_contains "deploy/compose.prod.yml" '${RUNTIME_ENV_FILE:-.env}'
assert_contains "deploy/compose.prod.yml" '${BACKEND_IMAGE:?Set BACKEND_IMAGE to an immutable image digest in release.env}'
assert_not_contains "deploy/compose.prod.yml" 'IMAGE_TAG'
assert_contains "deploy/scripts/deploy.sh" 'validate-runtime-env.sh'
assert_contains "deploy/README.md" '/etc/ridebook/ridebook.env'
assert_not_contains "deploy/README.md" 'cp .env.example .env'
assert_not_contains "deploy/README.md" 'docker compose -f compose.prod.yml'

validator="deploy/scripts/validate-runtime-env.sh"
[[ -x "$validator" ]] || fail "$validator must exist and be executable"

if "$validator" "deploy/.env.example" >/dev/null 2>&1; then
    fail "the production template must be rejected while it still contains placeholders"
fi

temporary_dir="$(mktemp -d)"
trap 'rm -rf "$temporary_dir"' EXIT
valid_env="$temporary_dir/runtime-valid.env"

cat > "$valid_env" <<'EOF'
DB_NAME=RideBookDB
DB_USERNAME=ridebook
DB_PASSWORD=test-db-password-123456
SECURITY_USER_NAME=admin
SECURITY_USER_PASSWORD=test-admin-password-123456
JWT_SIGNER_KEY=test-only-jwt-key-with-at-least-32-characters
FRONTEND_URL=https://ridebook.test
CORS_ALLOWED_ORIGINS=https://ridebook.test
GOOGLE_CLIENT_ID=test-google-client
GOOGLE_CLIENT_SECRET=test-google-secret
GOOGLE_REDIRECT_URI=https://api.ridebook.test/RideBook/login/oauth2/code/google
FACEBOOK_CLIENT_ID=test-facebook-client
FACEBOOK_CLIENT_SECRET=test-facebook-secret
FACEBOOK_REDIRECT_URI=https://api.ridebook.test/RideBook/login/oauth2/code/facebook
VNPAY_TMN_CODE=test-vnpay-code
VNPAY_HASH_SECRET=test-vnpay-secret
VNPAY_API_URL=https://sandbox.vnpayment.vn/paymentv2/vpcpay.html
VNPAY_RETURN_URL=https://api.ridebook.test/RideBook/api/v1/payment/vnpay-return
MOMO_PARTNER_CODE=test-momo-code
MOMO_ACCESS_KEY=test-momo-access
MOMO_SECRET_KEY=test-momo-secret
MOMO_API_URL=https://test-payment.momo.vn/v2/gateway/api/create
MOMO_RETURN_URL=https://api.ridebook.test/RideBook/api/v1/payment/momo-return
MOMO_NOTIFY_URL=https://api.ridebook.test/RideBook/api/v1/payment/momo-ipn
GOOGLE_MAPS_API_KEY=test-google-maps-key
FIREBASE_CONFIG_PATH=/etc/ridebook/firebase-service-account.json
EOF

"$validator" "$valid_env" >/dev/null

echo "Deployment contract checks passed."
