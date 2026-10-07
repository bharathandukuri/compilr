# Compilr — Utility & Deployment Scripts

All operational automation scripts are located in the [`scripts/`](../scripts) directory.

---

## Scripts Reference

| Script | Purpose | When to Use |
| :--- | :--- | :--- |
| **[`scripts/build-execution-images.sh`](../scripts/build-execution-images.sh)** | Builds the 14 sandbox execution Docker images in topological dependency order. | Before starting standard compose, or when updating language runtime images. |
| **[`scripts/deploy.sh`](../scripts/deploy.sh)** | Automated production deployment script for standard Docker Compose on host. | Deploying multi-container stack directly on a host or VPS without DinD. |
| **[`scripts/run-dind.sh`](../scripts/run-dind.sh)** | Runs the All-In-One DinD container on your host with optional port and image arguments. | Fast local testing or one-command VPS deployment of the DinD container. |
| **[`scripts/publish-dockerhub.sh`](../scripts/publish-dockerhub.sh)** | Automates building, tagging, and pushing the all-in-one image to Docker Hub. | Publishing public releases to Docker Hub for one-command distribution. |
| **[`scripts/entrypoint-dind.sh`](../scripts/entrypoint-dind.sh)** | Internal entrypoint executed inside the DinD container to start `dockerd`, sandbox networks, and compose. | Used automatically by `Dockerfile` during DinD container startup. |

---

## Detailed Usage Examples

### 1. `scripts/build-execution-images.sh`
Builds the 14 execution sandbox images in topological order with smart caching (skips existing images automatically):
```bash
# Fast verify / build missing images only (0.1s if cached):
./scripts/build-execution-images.sh

# Force rebuild all images without cache:
./scripts/build-execution-images.sh --force

# Build only a specific language (e.g. Python or Go):
./scripts/build-execution-images.sh python
./scripts/build-execution-images.sh go-1_22
```

### 2. `scripts/deploy.sh`
Checks host prerequisites, creates the isolated internal network `compilr-sandbox-net`, verifies/builds execution images, builds server and client containers, and launches services in the background:
```bash
./scripts/deploy.sh
```

### 3. `scripts/run-dind.sh`
Starts the All-In-One DinD container with host volume caching for images and data.
```bash
# Run on default port 6990 using local compilr:latest image:
./scripts/run-dind.sh

# Run on a custom host port (e.g. port 80):
./scripts/run-dind.sh 80

# Run a specific Docker Hub image on port 6990:
./scripts/run-dind.sh 6990 myuser/compilr:latest

# Reset and wipe cached volume for a fresh clean start:
./scripts/run-dind.sh 6990 compilr:latest --clean
```

### 4. `scripts/publish-dockerhub.sh`
Builds and pushes the DinD image to your public Docker Hub repository with BuildKit layer caching and optional sandbox pre-caching:
```bash
# Syntax: ./scripts/publish-dockerhub.sh <username>/<repo> [tag] [--embed-cache]
./scripts/publish-dockerhub.sh bharathandukuri/compilr latest

# Publish self-contained image with pre-baked execution sandbox archives:
./scripts/publish-dockerhub.sh bharathandukuri/compilr latest --embed-cache
```
