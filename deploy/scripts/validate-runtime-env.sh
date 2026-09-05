#!/usr/bin/env bash
set -euo pipefail

runtime_env_file="${1:-/etc/ridebook/ridebook.env}"
if [[ ! -f "$runtime_env_file" ]]; then
    echo "Runtime environment file not found: $runtime_env_file" >&2
    exit 1
fi

required_keys=(
    DB_NAME
    DB_USERNAME
    DB_PASSWORD
    SECURITY_USER_NAME
    SECURITY_USER_PASSWORD
    JWT_SIGNER_KEY
    FRONTEND_URL
    CORS_ALLOWED_ORIGINS
    GOOGLE_CLIENT_ID
    GOOGLE_CLIENT_SECRET
    GOOGLE_REDIRECT_URI
    FACEBOOK_CLIENT_ID
    FACEBOOK_CLIENT_SECRET
    FACEBOOK_REDIRECT_URI
    VNPAY_TMN_CODE
    VNPAY_HASH_SECRET
    VNPAY_API_URL
    VNPAY_RETURN_URL
    MOMO_PARTNER_CODE
    MOMO_ACCESS_KEY
    MOMO_SECRET_KEY
    MOMO_API_URL
    MOMO_RETURN_URL
    MOMO_NOTIFY_URL
    GOOGLE_MAPS_API_KEY
    FIREBASE_CONFIG_PATH
)

invalid_lines="$(awk '
    /^[[:space:]]*($|#)/ { next }
    !/^[A-Z][A-Z0-9_]*=.*/ { print NR }
' "$runtime_env_file")"
if [[ -n "$invalid_lines" ]]; then
    echo "Runtime environment file has invalid syntax at line(s): $invalid_lines" >&2
    exit 1
fi

missing_keys=()
duplicate_keys=()
for key in "${required_keys[@]}"; do
    count="$(grep -c "^${key}=" "$runtime_env_file" || true)"
    if [[ "$count" -eq 0 ]]; then
        missing_keys+=("$key")
    elif [[ "$count" -gt 1 ]]; then
        duplicate_keys+=("$key")
    elif ! grep -Eq "^${key}=.+" "$runtime_env_file"; then
        missing_keys+=("$key")
    fi
done

if ((${#missing_keys[@]} > 0)); then
    echo "Runtime environment is missing non-empty key(s): ${missing_keys[*]}" >&2
    exit 1
fi
if ((${#duplicate_keys[@]} > 0)); then
    echo "Runtime environment has duplicate key(s): ${duplicate_keys[*]}" >&2
    exit 1
fi

placeholder_keys="$(awk -F= '
    BEGIN { IGNORECASE = 1 }
    /^[[:space:]]*($|#)/ { next }
    $0 ~ /(replace[_-]?(with)?|your_|example\.com|smtp\.example)/ { print $1 }
' "$runtime_env_file")"
if [[ -n "$placeholder_keys" ]]; then
    echo "Runtime environment still contains placeholder value(s) for key(s):" >&2
    echo "$placeholder_keys" >&2
    exit 1
fi

if grep -Eq '^(BACKEND_IMAGE|IMAGE_TAG)=' "$runtime_env_file"; then
    echo "BACKEND_IMAGE and IMAGE_TAG belong in release.env, not the runtime environment file." >&2
    exit 1
fi

jwt_value="$(sed -n 's/^JWT_SIGNER_KEY=//p' "$runtime_env_file")"
if ((${#jwt_value} < 32)); then
    echo "JWT_SIGNER_KEY must contain at least 32 characters." >&2
    exit 1
fi

echo "Runtime environment validation passed: $runtime_env_file"
