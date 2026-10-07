#!/usr/bin/env bash
# ==============================================================================
# Compilr - Build and Publish All-In-One Image to Docker Hub
#
# Usage:
#   ./scripts/publish-dockerhub.sh <username>/<repository> [tag] [--embed-cache]
#
# Examples:
#   ./scripts/publish-dockerhub.sh bharathandukuri/compilr latest
#   ./scripts/publish-dockerhub.sh bharathandukuri/compilr latest --embed-cache
# ==============================================================================

set -euo pipefail

export DOCKER_BUILDKIT=1

IMAGE_REPO=""
IMAGE_TAG="latest"
EMBED_CACHE="false"

for ARG in "$@"; do
    case "$ARG" in
        --embed-cache|-c)
            EMBED_CACHE="true"
            ;;
        *:*)
            IMAGE_REPO="${ARG%%:*}"
            IMAGE_TAG="${ARG##*:}"
            ;;
        */*)
            IMAGE_REPO="$ARG"
            ;;
        *)
            if [ -z "$IMAGE_REPO" ]; then
                IMAGE_REPO="$ARG"
            else
                IMAGE_TAG="$ARG"
            fi
            ;;
    esac
done

if [ -z "${IMAGE_REPO}" ]; then
    echo "Usage: $0 <dockerhub-username>/<repo-name> [tag] [--embed-cache]"
    echo "Example: $0 bharathandukuri/compilr latest"
    exit 1
fi

FULL_IMAGE="${IMAGE_REPO}:${IMAGE_TAG}"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"

cd "${ROOT_DIR}"

echo "======================================================================"
echo "Publishing Compilr to Docker Hub"
echo "  • Target Image: ${FULL_IMAGE}"
echo "  • Embed Cache:  ${EMBED_CACHE}"
echo "  • Working Dir:  ${ROOT_DIR}"
echo "======================================================================"

# 1. Check Docker daemon
if ! docker info >/dev/null 2>&1; then
    echo "ERROR: Docker daemon is not running."
    exit 1
fi

# 2. Check Docker Hub authentication
echo "==> Verifying Docker Hub authentication..."
if ! docker system info | grep -iq "username"; then
    echo "Please log in to Docker Hub:"
    docker login
fi

# 3. Handle optional embedded sandbox cache
TEMP_CACHE_DIR="${ROOT_DIR}/.docker-cache-temp"
DOCKERFILE_TO_USE="Dockerfile"

if [ "${EMBED_CACHE}" = "true" ]; then
    echo ""
    echo "==> Preparing embedded image cache (pre-caching execution sandboxes)..."
    bash "${SCRIPT_DIR}/build-execution-images.sh"
    
    mkdir -p "${TEMP_CACHE_DIR}"
    echo "Exporting execution sandbox tarball..."
    docker save $(docker images --format '{{.Repository}}:{{.Tag}}' | grep '^execution/') -o "${TEMP_CACHE_DIR}/execution-sandboxes.tar"
    
    # Temporary Dockerfile referencing cached archive
    DOCKERFILE_TO_USE="Dockerfile.cached"
    cat <<EOF > "${DOCKERFILE_TO_USE}"
FROM docker:dind
RUN apk add --no-cache bash curl
WORKDIR /app
COPY .docker-cache-temp/*.tar /var/cache/docker-images/
COPY . /app
RUN rm -rf /app/.docker-cache-temp /app/Dockerfile.cached
RUN chmod +x /app/scripts/*.sh
ENV DOCKER_HOST=unix:///var/run/docker.sock DOCKER_TLS_CERTDIR=""
EXPOSE 6990 80
ENTRYPOINT ["/app/scripts/entrypoint-dind.sh"]
EOF
fi

cleanup() {
    rm -rf "${TEMP_CACHE_DIR}" "${ROOT_DIR}/Dockerfile.cached" 2>/dev/null || true
}
trap cleanup EXIT

# 4. Build the All-in-One image
echo ""
echo "==> Building ${FULL_IMAGE} from ${DOCKERFILE_TO_USE}..."
docker build -t "${FULL_IMAGE}" -f "${DOCKERFILE_TO_USE}" .

# 5. Push to Docker Hub
echo ""
echo "==> Pushing ${FULL_IMAGE} to Docker Hub..."
docker push "${FULL_IMAGE}"

echo ""
echo "======================================================================"
echo "Successfully published ${FULL_IMAGE} to Docker Hub!"
echo "======================================================================"
echo ""
echo "Anyone can run Compilr with a single command:"
echo ""
echo "  docker run -d \\"
echo "    --name compilr \\"
echo "    --privileged \\"
echo "    -p 6990:6990 \\"
echo "    -v compilr-docker-data:/var/lib/docker \\"
echo "    --restart unless-stopped \\"
echo "    ${FULL_IMAGE}"
echo ""
echo "Access Compilr at: http://<ip>:6990"
echo "======================================================================"
