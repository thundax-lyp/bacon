#!/bin/zsh

set -euo pipefail

SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
REPO_ROOT=$(cd "$SCRIPT_DIR/.." && pwd)

cd "$REPO_ROOT"

mvn -pl bacon-app/bacon-mono-boot -am -DskipTests install
mvn -pl bacon-app/bacon-mono-boot spring-boot:run
