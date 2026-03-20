#!/usr/bin/env bash
set -e
DB_USER=${1:-root}
DB_PASS=${2:-123456}
DB_NAME=${3:-ocms}
mysql -u"$DB_USER" -p"$DB_PASS" -e "CREATE DATABASE IF NOT EXISTS \\`$DB_NAME\\` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
mysql -u"$DB_USER" -p"$DB_PASS" "$DB_NAME" < "$(dirname "$0")/sql/schema.sql"
mysql -u"$DB_USER" -p"$DB_PASS" "$DB_NAME" < "$(dirname "$0")/sql/data.sql"
echo "[OCMS] database initialized: $DB_NAME"
