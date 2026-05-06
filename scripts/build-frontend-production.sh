#!/usr/bin/env bash
set -euo pipefail

flutter build web --dart-define=NAROO_API_BASE_URL=https://api.naroo.app
