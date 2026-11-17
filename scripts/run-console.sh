#!/usr/bin/env bash
# Menjalankan pengujian lapisan model/DAO/service tanpa GUI.
set -euo pipefail
cd "$(dirname "$0")/.."

./scripts/compile.sh
java -cp "out:lib/sqlite-jdbc-3.47.1.0.jar" parkir.app.MainUji
