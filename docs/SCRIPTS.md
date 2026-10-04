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
Builds all 14 execution sandbox images (Isolate 1.0, Java 21, Kotlin 1.9, Node.js 20, TypeScript 5.4, Python 3.12, C 17, C++ 23, Go 1.22, Rust 1.75, PostgreSQL 16, MySQL 8.0, SQLite 3, MongoDB 8.0):
```bash
./scripts/build-execution-images.sh
```

### 2. `scripts/deploy.sh`
Checks host prerequisites, creates the isolated internal network `compilr-sandbox-net`, builds execution images, builds server and client containers, and launches services in the background:
```bash
./scripts/deploy.sh
```

### 3. `scripts/run-dind.sh`
Starts the All-In-One DinD container.
```bash
# Run on default port 6990 using local compilr:latest image:
./scripts/run-dind.sh

# Run on a custom host port (e.g. port 80):
./scripts/run-dind.sh 80

# Run a specific Docker Hub image on port 6990:
./scripts/run-dind.sh 6990 myuser/compilr:latest
```

### 4. `scripts/publish-dockerhub.sh`
Builds and pushes the DinD image to your public Docker Hub repository:
```bash
# Syntax: ./scripts/publish-dockerhub.sh <username>/<repo> [tag]
./scripts/publish-dockerhub.sh bharathandukuri/compilr latest
```
