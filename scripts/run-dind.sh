#!/usr/bin/env bash
# ==============================================================================
# Compilr - Run DinD Container Locally or on Server
#
# Usage:
#   ./scripts/run-dind.sh [port] [image-name] [--clean]
#
# Examples:
#   ./scripts/run-dind.sh                             # Runs local compilr:latest on port 6990
#   ./scripts/run-dind.sh 80                          # Runs on port 80
#   ./scripts/run-dind.sh 6990 compilr:latest --clean # Cleans cached volume and restarts
# ==============================================================================

set -euo pipefail

PORT="6990"
CONTAINER_NAME="compilr"
CLEAN_CACHE="false"
IMAGE="${IMAGE_NAME:-compilr:latest}"

# Parse arguments
for ARG in "$@"; do
    case "$ARG" in
        --clean|-c)
            CLEAN_CACHE="true"
            ;;
        [0-9]*)
            PORT="$ARG"
            ;;
        *:*|*/*)
            IMAGE="$ARG"
            ;;
    esac
done

echo "======================================================================"
echo "Starting Compilr All-In-One Container"
echo "  • Image:     ${IMAGE}"
echo "  • Host Port: ${PORT}"
echo "  • Volume:    compilr-docker-data (caches images & data)"
echo "======================================================================"

# Handle clean request
if [ "${CLEAN_CACHE}" = "true" ]; then
    echo "==> Cleaning existing container and cached volume..."
    docker rm -f "${CONTAINER_NAME}" >/dev/null 2>&1 || true
    docker volume rm -f compilr-docker-data >/dev/null 2>&1 || true
fi

# Remove existing container if present
if docker ps -a --format '{{.Names}}' | grep -Eq "^${CONTAINER_NAME}\$"; then
    echo "Stopping and removing existing container '${CONTAINER_NAME}'..."
    docker rm -f "${CONTAINER_NAME}" >/dev/null 2>&1 || true
fi

# Resolve environment file
ENV_ARG=""
if [ -f ".env" ]; then
    echo "Using configuration from .env file..."
    ENV_ARG="--env-file .env"
elif [ -f ".env.example" ]; then
    echo "No .env found. Using default internal configuration..."
fi

# Run DinD container
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
echo "  • Clean reset: ./scripts/run-dind.sh ${PORT} ${IMAGE} --clean"
echo "======================================================================"
