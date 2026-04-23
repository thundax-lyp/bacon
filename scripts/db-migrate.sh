#!/bin/zsh

set -euo pipefail

SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
REPO_ROOT=$(cd "$SCRIPT_DIR/.." && pwd)

cd "$REPO_ROOT"

if ! command -v mysql >/dev/null 2>&1; then
    echo "mysql client is required" >&2
    exit 1
fi

DB_HOST=${BACON_DB_HOST:-}
DB_PORT=${BACON_DB_PORT:-}
DB_NAME=${BACON_DB_NAME:-}
DB_USERNAME=${BACON_DB_USERNAME:-}
DB_PASSWORD=${BACON_DB_PASSWORD:-}

if [[ -n "${BACON_DB_URL:-}" ]]; then
    DB_URL_WITHOUT_QUERY=${BACON_DB_URL%%\?*}
    DB_URL_WITHOUT_PREFIX=${DB_URL_WITHOUT_QUERY#jdbc:mysql://}
    DB_HOST_PORT=${DB_URL_WITHOUT_PREFIX%%/*}
    DB_NAME_FROM_URL=${DB_URL_WITHOUT_PREFIX#*/}
    if [[ "$DB_HOST_PORT" == *":"* ]]; then
        DB_HOST_FROM_URL=${DB_HOST_PORT%%:*}
        DB_PORT_FROM_URL=${DB_HOST_PORT##*:}
    else
        DB_HOST_FROM_URL=$DB_HOST_PORT
        DB_PORT_FROM_URL=3306
    fi
    DB_HOST=${DB_HOST:-$DB_HOST_FROM_URL}
    DB_PORT=${DB_PORT:-$DB_PORT_FROM_URL}
    DB_NAME=${DB_NAME:-$DB_NAME_FROM_URL}
fi

DB_HOST=${DB_HOST:-127.0.0.1}
DB_PORT=${DB_PORT:-3306}

if [[ -z "$DB_NAME" ]]; then
    echo "BACON_DB_NAME or BACON_DB_URL is required" >&2
    exit 1
fi

if [[ -z "$DB_USERNAME" ]]; then
    echo "BACON_DB_USERNAME is required" >&2
    exit 1
fi

MYSQL_ARGS=(-h "$DB_HOST" -P "$DB_PORT" -u "$DB_USERNAME" --default-character-set=utf8mb4 "$DB_NAME")
if [[ -n "$DB_PASSWORD" ]]; then
    MYSQL_ARGS=(-p"$DB_PASSWORD" "${MYSQL_ARGS[@]}")
fi

run_sql() {
    local file=$1
    if [[ ! -f "$file" ]]; then
        return
    fi
    echo "Running $file"
    mysql "${MYSQL_ARGS[@]}" < "$file"
}

FILES=(
    db/schema/upms.sql
    db/data/upms.sql
    db/schema/auth.sql
    db/data/auth.sql
    db/schema/order.sql
    db/schema/inventory.sql
    db/data/inventory.sql
    db/schema/payment.sql
    db/data/payment.sql
    db/schema/storage.sql
    db/data/storage.sql
)

for file in "${FILES[@]}"; do
    run_sql "$file"
done

echo "Database migration completed for $DB_NAME at $DB_HOST:$DB_PORT"
