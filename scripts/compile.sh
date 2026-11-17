#!/usr/bin/env bash
# Mengompilasi seluruh kode ke folder out/.
set -euo pipefail
cd "$(dirname "$0")/.."

JAR="lib/sqlite-jdbc-3.47.1.0.jar"
if [ ! -f "$JAR" ]; then
  echo "Driver SQLite tidak ditemukan di $JAR" >&2
  exit 1
fi

rm -rf out
mkdir -p out
javac --release 22 -encoding UTF-8 -cp "$JAR" -d out $(find src -name '*.java')
echo "Kompilasi selesai: $(find out -name '*.class' | wc -l | tr -d ' ') berkas kelas."
