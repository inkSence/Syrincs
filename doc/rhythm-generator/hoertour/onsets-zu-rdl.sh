#!/usr/bin/env bash
# Lokaler Hörhelfer zur Anleitung; benötigt den Paketbau und JDK 21.
set -euo pipefail

tour_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
repo_dir="$(cd -- "$tour_dir/../../.." && pwd)"

if [[ ! -f "$repo_dir/target/classes/syrincs/a_domain/rhythm/HuffmanRhythm.class" ]]; then
    echo 'Bitte zuerst im Repository mvn package -Dsyrincs.skipCompletionInstall=true ausführen.' >&2
    exit 1
fi

exec java --class-path "$repo_dir/target/classes" "$tour_dir/OnsetsToRdl.java" "$@"
