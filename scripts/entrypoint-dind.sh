#!/usr/bin/env bash
# ==============================================================================
# Compilr All-In-One (DinD) Entrypoint Script
#
# Starts internal Docker daemon (dockerd), ensures required networks exist,
# loads pre-cached image archives, verifies sandbox images, and runs Compose stack.
# ==============================================================================

set -euo pipefail

# Force Docker CLI in this container to use the internal Unix socket
export DOCKER_HOST="unix:///var/run/docker.sock"
export DOCKER_TLS_CERTDIR=""
export DOCKER_BUILDKIT=1

# Graceful termination handler
cleanup() {
    echo ""
    echo "======================================================================"
    echo "Stopping Compilr services..."
    echo "======================================================================"
    docker compose down --remove-orphans 2>/dev/null || true

    echo "Stopping internal Docker daemon..."
    if [ -n "${DOCKERD_PID:-}" ]; then
        kill -TERM "${DOCKERD_PID}" 2>/dev/null || true
        wait "${DOCKERD_PID}" 2>/dev/null || true
    fi
    echo "Compilr stopped cleanly."
    exit 0
}

trap cleanup SIGINT SIGTERM

echo "======================================================================"
echo "Starting Compilr All-In-One (Docker-in-Docker)"
echo "======================================================================"

# 1. Start internal dockerd
echo "==> Starting internal Docker daemon (dockerd)..."
find /run /var/run -iname 'docker*.pid' -delete 2>/dev/null || true
dockerd --storage-driver=overlayfs >/var/log/dockerd.log 2>&1 &
DOCKERD_PID=$!

# 2. Wait for dockerd to be ready
echo "==> Waiting for Docker daemon to become responsive..."
TIMEOUT=60
START=$(date +%s)
while ! docker info >/dev/null 2>&1; do
    if [ $(($(date +%s) - START)) -gt $TIMEOUT ]; then
        echo "ERROR: Internal Docker daemon did not start within ${TIMEOUT}s."
        echo "--- Daemon log output: ---"
        cat /var/log/dockerd.log
        exit 1
    fi
    sleep 0.5
done
echo "==> Internal Docker daemon is ready."

# 3. Load pre-cached image archives (if embedded into image or mounted)
CACHE_DIRS=("/var/cache/docker-images" "/docker-cache" "/app/cache/images")
for CDIR in "${CACHE_DIRS[@]}"; do
    if [ -d "$CDIR" ]; then
        for ARCHIVE in "$CDIR"/*.tar "$CDIR"/*.tar.gz; do
            if [ -f "$ARCHIVE" ]; then
                echo "==> Loading pre-cached Docker image: ${ARCHIVE}..."
                docker load -i "$ARCHIVE" 2>/dev/null || true
            fi
        done
    fi
done

# 4. Create isolated internal execution network for backend sandboxes
echo "==> Ensuring isolated sandbox network 'compilr-sandbox-net' exists..."
if ! docker network inspect compilr-sandbox-net >/dev/null 2>&1; then
    docker network create --internal --driver bridge compilr-sandbox-net
    echo "Created internal Docker network: compilr-sandbox-net"
fi

# 5. Prepare .env if not present
if [ ! -f .env ] && [ -f .env.example ]; then
    cp .env.example .env
fi

# 6. Verify or build sandbox execution images
if [ "${BUILD_EXECUTION_IMAGES:-true}" = "true" ]; then
    echo "==> Verifying execution sandbox images..."
    if [ -f "scripts/build-execution-images.sh" ]; then
        bash "scripts/build-execution-images.sh"
    fi
fi

# 7. Launch Docker Compose stack
echo ""
echo "======================================================================"
echo "Compilr Services Starting (Client UI on Port ${CLIENT_PORT:-6990})"
echo "======================================================================"
echo ""

if [ "$#" -gt 0 ]; then
    exec "$@"
elif [ "${FORCE_REBUILD:-false}" = "true" ]; then
    docker compose up --build &
    COMPOSE_PID=$!
    wait "$COMPOSE_PID"
else
    docker compose up &
    COMPOSE_PID=$!
    wait "$COMPOSE_PID"
fi
