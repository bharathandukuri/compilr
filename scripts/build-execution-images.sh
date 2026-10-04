#!/usr/bin/env bash
# ==============================================================================
# Compilr - Build All Execution Sandbox Images
#
# Builds the 17 Docker images required by the Compilr execution engine.
# Note: 'execution/isolate:1.0' is the base image and MUST be built first.
# ==============================================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
DOCKER_DIR="${ROOT_DIR}/server/src/main/resources/docker"

echo "======================================================================"
echo "Compilr: Building Execution Sandbox Docker Images"
echo "Root directory: ${ROOT_DIR}"
echo "Docker contexts: ${DOCKER_DIR}"
echo "======================================================================"

# Step 1: Base Isolate Sandbox
echo ""
echo "[1/17] Building base sandbox image: execution/isolate:1.0..."
docker build -t execution/isolate:1.0 "${DOCKER_DIR}/isolate-1_0"

# Step 2: Programming Languages & Databases
declare -A IMAGES=(
    ["execution/c:17"]="c-17"
    ["execution/cpp:23"]="cpp-23"
    ["execution/csharp:12"]="csharp-12"
    ["execution/dart:3.4"]="dart-3_4"
    ["execution/go:1.22"]="go-1_22"
    ["execution/java:21"]="java-21"
    ["execution/javascript:node-20"]="javascript-node-20"
    ["execution/kotlin:1.9"]="kotlin-1_9"
    ["execution/mongodb:8.0"]="mongodb-8_0"
    ["execution/mysql:8.0"]="mysql-8_0"
    ["execution/php:8.3"]="php-8_3"
    ["execution/postgres:16"]="postgres-16"
    ["execution/python:3.12"]="python-3_12"
    ["execution/rust:1.75"]="rust-1_75"
    ["execution/sqlite:3"]="sqlite-3"
    ["execution/typescript:5.4"]="typescript-5_4"
)

INDEX=2
for TAG in "${!IMAGES[@]}"; do
    SUBDIR="${IMAGES[$TAG]}"
    echo ""
    echo "[${INDEX}/17] Building ${TAG} from ${SUBDIR}..."
    docker build -t "${TAG}" "${DOCKER_DIR}/${SUBDIR}"
    INDEX=$((INDEX + 1))
done

echo ""
echo "======================================================================"
echo "All 17 Compilr execution images built successfully!"
echo "Verify with: docker images 'execution/*'"
echo "======================================================================"
