#!/usr/bin/env bash
# Menjalankan aplikasi desktop Eparking.
set -euo pipefail
cd "$(dirname "$0")/.."

# Pemisah classpath mengikuti OS: ';' di Windows, ':' di POSIX.
SEP=":"
case "$(uname -s)" in MINGW*|MSYS*|CYGWIN*) SEP=";" ;; esac

./scripts/compile.sh
java -cp "out${SEP}lib/sqlite-jdbc-3.47.1.0.jar" parkir.app.MainApp
