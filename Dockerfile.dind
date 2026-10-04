# ==============================================================================
# Compilr All-In-One Dockerfile (Docker-in-Docker)
#
# Runs the entire Compilr stack (Client, Server, Redis, and Sandbox Containers)
# inside a single self-contained container.
# ==============================================================================

FROM docker:dind

# Install bash for build scripts and curl for healthchecks
RUN apk add --no-cache bash curl

WORKDIR /app

# Copy application source code and configuration
COPY . /app

# Ensure all scripts are executable
RUN chmod +x /app/scripts/*.sh

# Expose Client UI port (6990 default, 80 standard HTTP)
EXPOSE 6990 80

ENTRYPOINT ["/app/scripts/entrypoint-dind.sh"]
CMD ["docker", "compose", "up", "--build"]
