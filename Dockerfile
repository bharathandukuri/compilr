# ==============================================================================
# Compilr All-In-One Dockerfile (Docker-in-Docker)
#
# Runs the entire Compilr stack (Client, Server, Redis, and Sandbox Containers)
# inside a single self-contained container.
# ==============================================================================

FROM docker:dind

# Install runtime utilities for DinD orchestration and health probes
RUN apk add --no-cache bash curl

WORKDIR /app

# Directory for optional pre-baked or cached Docker image tar archives
RUN mkdir -p /var/cache/docker-images

# Copy application repository
COPY . /app

# Ensure scripts have execute permissions
RUN chmod +x /app/scripts/*.sh

# Internal DinD daemon environment
ENV DOCKER_HOST=unix:///var/run/docker.sock \
    DOCKER_TLS_CERTDIR=""

# Expose Client UI port (6990 default, 80 standard HTTP)
EXPOSE 6990 80

ENTRYPOINT ["/app/scripts/entrypoint-dind.sh"]
