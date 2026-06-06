#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd -- "$SCRIPT_DIR/.." && pwd)"

DB_USER="${SYRINCS_DB_USER:-syrincs}"
DB_NAME="${SYRINCS_DB_NAME:-hindemith}"
DB_PASSWORD="${SYRINCS_DB_PASSWORD:-syrincs}"
RESET="${SYRINCS_DB_RESET:-0}"

if [[ "${1:-}" == "--help" || "${1:-}" == "-h" ]]; then
  cat <<'USAGE'
Usage:
  bash scripts/init-postgres.sh

Environment:
  SYRINCS_DB_USER      Database role to create/use. Default: syrincs
  SYRINCS_DB_NAME      Database name to create/use. Default: hindemith
  SYRINCS_DB_PASSWORD  Password to set for the role. Default: syrincs
  SYRINCS_DB_RESET     Set to 1 to drop and recreate the database

This wrapper keeps the privileged PostgreSQL steps explicit and then runs
`syrincs init` to create the application tables.
USAGE
  exit 0
fi

if ! command -v psql >/dev/null 2>&1; then
  echo "Cannot find psql. Install PostgreSQL first." >&2
  exit 127
fi

if ! command -v sudo >/dev/null 2>&1; then
  echo "Cannot find sudo. Run the PostgreSQL commands manually." >&2
  exit 127
fi

echo "[PG] Ensuring role '$DB_USER' exists and has the configured password."
sudo -u postgres psql -v ON_ERROR_STOP=1 \
  -v db_user="$DB_USER" \
  -v db_password="$DB_PASSWORD" \
  -d postgres <<'SQL'
SELECT CASE
    WHEN EXISTS (SELECT 1 FROM pg_roles WHERE rolname = :'db_user')
    THEN format('ALTER ROLE %I WITH LOGIN PASSWORD %L', :'db_user', :'db_password')
    ELSE format('CREATE ROLE %I LOGIN PASSWORD %L', :'db_user', :'db_password')
END;
\gexec
SQL

if [[ "$RESET" == "1" ]]; then
  echo "[PG] Reset requested, dropping database '$DB_NAME' if it exists."
  sudo -u postgres dropdb --if-exists --force "$DB_NAME"
fi

if sudo -u postgres psql -v db_name="$DB_NAME" -tAc "SELECT 1 FROM pg_database WHERE datname = :'db_name'" | grep -q 1; then
  echo "[PG] Database '$DB_NAME' already exists."
else
  echo "[PG] Creating database '$DB_NAME'."
  sudo -u postgres createdb -O "$DB_USER" "$DB_NAME"
fi

echo "[PG] Ensuring public schema ownership."
sudo -u postgres psql -v ON_ERROR_STOP=1 -v db_user="$DB_USER" -d "$DB_NAME" <<'SQL'
SELECT format('ALTER SCHEMA public OWNER TO %I', :'db_user');
\gexec
SQL

export SYRINCS_DB_URL="${SYRINCS_DB_URL:-jdbc:postgresql://localhost:5432/$DB_NAME}"
export SYRINCS_DB_USER="${SYRINCS_DB_USER:-$DB_USER}"
export SYRINCS_DB_PASSWORD="${SYRINCS_DB_PASSWORD:-$DB_PASSWORD}"
export HINDEMITH_DB_URL="${HINDEMITH_DB_URL:-$SYRINCS_DB_URL}"
export HINDEMITH_DB_USER="${HINDEMITH_DB_USER:-$SYRINCS_DB_USER}"
export HINDEMITH_DB_PASSWORD="${HINDEMITH_DB_PASSWORD:-$SYRINCS_DB_PASSWORD}"

if [[ -x "$PROJECT_ROOT/target/app/bin/syrincs" ]]; then
  "$PROJECT_ROOT/target/app/bin/syrincs" init
elif command -v syrincs >/dev/null 2>&1; then
  syrincs init
else
  mvn -q -DskipTests compile exec:java -Dexec.args=init
fi
