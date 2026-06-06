#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd -- "$SCRIPT_DIR/.." && pwd)"

SC_LANG="${SC_LANG:-sclang}"
SC_SCRIPT="${SC_SCRIPT:-$PROJECT_ROOT/supercollider/syrincs_osc_consumer.scd}"

if [[ "${1:-}" == "--help" || "${1:-}" == "-h" ]]; then
  cat <<'USAGE'
Usage:
  bash scripts/start-supercollider-consumer.sh

Environment:
  SC_LANG     SuperCollider language executable. Default: sclang
  SC_SCRIPT   SuperCollider script to execute.

The process runs in the foreground. Stop it with Ctrl+C.
USAGE
  exit 0
fi

if ! command -v "$SC_LANG" >/dev/null 2>&1; then
  echo "Cannot find '$SC_LANG'. Install SuperCollider or set SC_LANG=/path/to/sclang." >&2
  exit 127
fi

if [[ ! -f "$SC_SCRIPT" ]]; then
  echo "Cannot find SuperCollider script: $SC_SCRIPT" >&2
  exit 1
fi

echo "[SC] Starting Syrincs SuperCollider consumer"
echo "[SC] Script: $SC_SCRIPT"
echo "[SC] Stop with Ctrl+C"

cd "$PROJECT_ROOT"
exec "$SC_LANG" "$SC_SCRIPT"
