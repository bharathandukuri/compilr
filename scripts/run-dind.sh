#!/usr/bin/env bash
# ==============================================================================
# Compilr - Run DinD Container Locally or on Server
#
# Usage:
#   ./scripts/run-dind.sh [port] [image-name]
#
# Examples:
#   ./scripts/run-dind.sh                     # Runs on default port 6990
#   ./scripts/run-dind.sh 80                  # Runs on port 80
#   ./scripts/run-dind.sh 6990 myuser/compilr # Runs specific image on port 6990
# ==============================================================================

set -euo pipefail

PORT="${1:-6990}"
IMAGE="${2:-compilr:latest}"
CONTAINER_NAME="compilr"

echo "======================================================================"
echo "Starting Compilr All-In-One Container"
echo "  • Image:     ${IMAGE}"
echo "  • Host Port: ${PORT}"
echo "======================================================================"

# Remove existing container if present
if docker ps -a --format '{{.Names}}' | grep -Eq "^${CONTAINER_NAME}\$"; then
    echo "Stopping and removing existing container '${CONTAINER_NAME}'..."
    docker rm -f "${CONTAINER_NAME}" >/dev/null 2>&1 || true
fi

ENV_ARG=""
if [ -f ".env" ]; then
    echo "Using configuration from .env file..."
    ENV_ARG="--env-file .env"
elif [ -f ".env.example" ]; then
    echo "No .env found. Using default internal configuration..."
fi

# Run container in background
docker run -d \
    --name "${CONTAINER_NAME}" \
    --privileged \
    -p "${PORT}:6990" \
    ${ENV_ARG} \
    -v compilr-docker-data:/var/lib/docker \
    --restart unless-stopped \
    "${IMAGE}"

echo ""
echo "======================================================================"
echo "Compilr container '${CONTAINER_NAME}' launched!"
echo "  • Access UI:   http://localhost:${PORT}"
echo "  • View logs:   docker logs -f ${CONTAINER_NAME}"
echo "  • Stop:        docker stop ${CONTAINER_NAME}"
echo "======================================================================"
