#!/usr/bin/env bash
set -euo pipefail

: "${NAROO_API_BASE_URL:?Set NAROO_API_BASE_URL to an https backend URL before building.}"

script_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
backend_dir="$(cd -- "$script_dir/.." && pwd)"
workspace_dir="$(cd -- "$backend_dir/.." && pwd)"
frontend_dir="${NAROO_FRONTEND_DIR:-$workspace_dir/frontend}"
build_profile="${NAROO_BUILD_PROFILE:-production}"

if [ ! -f "$frontend_dir/pubspec.yaml" ]; then
  echo "Could not find Flutter frontend at $frontend_dir" >&2
  exit 1
fi

case "$NAROO_API_BASE_URL" in
  https://*) ;;
  *)
    echo "NAROO_API_BASE_URL must start with https:// for production builds" >&2
    exit 1
    ;;
esac

build_args=(
  build
  web
  --release
  "--dart-define=NAROO_BUILD_PROFILE=$build_profile"
  "--dart-define=NAROO_API_BASE_URL=$NAROO_API_BASE_URL"
)

if [ -n "${NAROO_WEB_BASE_HREF:-}" ]; then
  build_args+=(--base-href "$NAROO_WEB_BASE_HREF")
fi

(
  cd "$frontend_dir"
  flutter "${build_args[@]}"
)
