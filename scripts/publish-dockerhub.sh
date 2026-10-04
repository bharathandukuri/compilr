#!/usr/bin/env bash
# ==============================================================================
# Compilr - Build and Publish All-In-One Image to Docker Hub
#
# Usage:
#   ./scripts/publish-dockerhub.sh <dockerhub-username>/<repository-name> [tag]
#
# Example:
#   ./scripts/publish-dockerhub.sh bharathandukuri/compilr latest
# ==============================================================================

set -euo pipefail

IMAGE_REPO="${1:-}"
IMAGE_TAG="${2:-latest}"

if [ -z "${IMAGE_REPO}" ]; then
    echo "Usage: $0 <dockerhub-username>/<repo-name> [tag]"
    echo "Example: $0 bharathandukuri/compilr latest"
    exit 1
fi

FULL_IMAGE="${IMAGE_REPO}:${IMAGE_TAG}"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"

cd "${ROOT_DIR}"

echo "======================================================================"
echo "Publishing Compilr to Docker Hub"
echo "Target Image: ${FULL_IMAGE}"
echo "Context:      ${ROOT_DIR}"
echo "======================================================================"

# 1. Check Docker daemon
if ! docker info >/dev/null 2>&1; then
    echo "ERROR: Docker daemon is not running."
    exit 1
fi

# 2. Check Docker Hub authentication
echo "Verifying Docker Hub login..."
if ! docker system info | grep -iq "username"; then
    echo "Please log in to Docker Hub:"
    docker login
fi

# 3. Build the All-in-One image
echo ""
echo "==> Building ${FULL_IMAGE} from Dockerfile..."
docker build -t "${FULL_IMAGE}" -f Dockerfile .

# 4. Push to Docker Hub
echo ""
echo "==> Pushing ${FULL_IMAGE} to Docker Hub..."
docker push "${FULL_IMAGE}"

echo ""
echo "======================================================================"
echo "Successfully pushed ${FULL_IMAGE} to Docker Hub!"
echo "======================================================================"
echo ""
echo "Anyone can now run Compilr with a single command:"
echo ""
echo "  docker run -d \\"
echo "    --name compilr \\"
echo "    --privileged \\"
echo "    -p 6990:6990 \\"
echo "    -v compilr-data:/var/lib/docker \\"
echo "    --restart unless-stopped \\"
echo "    ${FULL_IMAGE}"
echo ""
echo "Access Compilr at: http://<ip>:6990"
echo "======================================================================"
