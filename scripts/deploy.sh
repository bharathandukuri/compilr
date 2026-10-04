#!/usr/bin/env bash
# ==============================================================================
# Compilr - Production Deployment Script for VPS
# ==============================================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"

cd "${ROOT_DIR}"

echo "======================================================================"
echo "Compilr Production Deployment"
echo "Working directory: ${ROOT_DIR}"
echo "======================================================================"

# 1. Verify Docker environment
if ! command -v docker &>/dev/null; then
    echo "ERROR: Docker is not installed. Please install Docker before deploying."
    exit 1
fi

if ! docker info &>/dev/null; then
    echo "ERROR: Docker daemon is not running or current user lacks docker permissions."
    exit 1
fi

# 2. Prepare environment configuration
if [ ! -f ".env" ]; then
    echo "Creating .env from .env.example..."
    cp .env.example .env
fi

# 3. Create isolated internal execution network for backend sandboxes
echo "Ensuring isolated sandbox network 'compilr-sandbox-net' exists..."
if ! docker network inspect compilr-sandbox-net &>/dev/null; then
    docker network create --internal --driver bridge compilr-sandbox-net
    echo "Created internal Docker network: compilr-sandbox-net"
else
    echo "Isolated sandbox network 'compilr-sandbox-net' already exists."
fi

# 4. Build all 17 sandbox execution images
echo ""
echo "Building execution sandbox images..."
bash "${SCRIPT_DIR}/build-execution-images.sh"

# 5. Build and launch Docker Compose services
echo ""
echo "Building and starting Compilr services (Client: 6990, Server: 6991, Redis: 6992)..."
docker compose --build server
docker compose --build client
docker compose up

# 6. Verification and status display
echo ""
echo "======================================================================"
echo "Compilr Deployment Successful!"
echo "======================================================================"
echo "Services status:"
docker compose ps

echo ""
echo "Port mappings:"
echo "  • Client (Frontend UI):  http://<vps-ip>:6990"
echo "  • Server (API & Docs):   http://<vps-ip>:6991  (Swagger: http://<vps-ip>:6991/swagger-ui.html)"
echo "  • Redis (Auth Queue):    localhost:6992"
echo ""
echo "Execution security:"
echo "  • Sandbox containers run in network mode 'none' or 'compilr-sandbox-net' (--internal)."
echo "  • Untrusted user code CANNOT access host exposed ports (6990, 6991, 6992)."
echo ""
echo "Useful commands:"
echo "  • View logs:    docker compose logs -f"
echo "  • Check health: docker compose ps"
echo "  • Stop stack:   docker compose down"
echo "======================================================================"
