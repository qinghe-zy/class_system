#!/usr/bin/env bash
set -e
cd "$(dirname "$0")/backend"
echo "[OCMS] starting backend..."
mvn spring-boot:run
