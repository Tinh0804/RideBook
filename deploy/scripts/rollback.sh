#!/usr/bin/env bash
set -euo pipefail

script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
state_dir="${RIDEBOOK_STATE_DIR:-/opt/ridebook/state}"
previous_image_file="$state_dir/previous-image"

if [[ ! -s "$previous_image_file" ]]; then
    echo "No previous Backend image has been recorded." >&2
    exit 1
fi

previous_image="$(tr -d '[:space:]' < "$previous_image_file")"
exec "$script_dir/deploy.sh" "$previous_image"
