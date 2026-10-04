# Compilr — Deployment Guide

Compilr supports two primary deployment patterns:
1. **With DinD (All-In-One)**: Single self-contained Docker container running the entire stack (Client, Server, Redis, and inner Docker daemon).
2. **Without DinD (Standard Compose)**: Multi-container setup running directly on the host Docker engine via `docker compose`.

---

## Method 1: Deploy With DinD (All-In-One Container)

**Best for**: Fast deployments, cloud VPS instances, zero host-socket exposure, and pre-packaged Docker Hub distribution.

In this mode, everything runs inside a single privileged container:
* Internal `dockerd` daemon manages sandbox containers and compose services.
* The host only sees **one** running container.
* No need to mount the host's `/var/run/docker.sock`.

### 1. Build the All-in-One Image
From the project root:
```bash
docker build -t compilr:latest .
```

### 2. Run the Container
```bash
docker run -d \
  --name compilr \
  --privileged \
  -p 6990:6990 \
  -v compilr-docker-data:/var/lib/docker \
  --restart unless-stopped \
  compilr:latest
```

*To customize settings with a `.env` file:*
```bash
docker run -d \
  --name compilr \
  --privileged \
  -p 6990:6990 \
  --env-file .env \
  -v compilr-docker-data:/var/lib/docker \
  --restart unless-stopped \
  compilr:latest
```

> [!TIP]
> The volume `-v compilr-docker-data:/var/lib/docker` ensures that all downloaded runtime layers and built sandbox images are persisted on your host disk. Initial boot builds the images; all subsequent container restarts start up in seconds.

### 3. Access the Application
Open your browser at:
```text
http://<server-ip>:6990
```

---

## Method 2: Deploy Without DinD (Standard Docker Compose)

**Best for**: Development environments, machines where `--privileged` is restricted, or when using an existing Docker host.

### 1. Automated VPS Deployment
Run the included deployment script:
```bash
./scripts/deploy.sh
```

### 2. Manual Step-by-Step
```bash
# 1. Create .env if you wish to override limits or ports:
cp .env.example .env

# 2. Build sandbox execution images:
./scripts/build-execution-images.sh

# 3. Start services in the background:
docker compose up -d --build
```

### 3. Port Mappings
| Service | Default Port | URL / Access |
| :--- | :--- | :--- |
| **Client UI** | `6990` | `http://<server-ip>:6990` |
| **Server API** | `6991` | `http://<server-ip>:6991` (Swagger: `http://<server-ip>:6991/swagger-ui.html`) |
| **Redis** | `6992` | `localhost:6992` |

---

## Method 3: Publish to Docker Hub & Public Distribution

You can publish the all-in-one image to Docker Hub so anyone can run your instance with a single command.

### 1. Log in to Docker Hub
```bash
docker login
```

### 2. Publish using the helper script
Replace `<your-dockerhub-username>` with your Docker Hub handle:
```bash
./scripts/publish-dockerhub.sh <your-dockerhub-username>/compilr latest
```

### 3. Public Single-Command Run
Once published, anyone in the world can run Compilr by executing:
```bash
docker run -d \
  --name compilr \
  --privileged \
  -p 6990:6990 \
  -v compilr-data:/var/lib/docker \
  --restart unless-stopped \
  <your-dockerhub-username>/compilr:latest
```

---

## Production & Cloud VPS Guidelines (e.g. AWS EC2)

1. **Firewall / Security Group**:
   - Inbound rule: Allow TCP port `6990` from `0.0.0.0/0` (or your chosen IP range) for the Web IDE.
   - Optional: Allow TCP port `6991` only if you need public access to the raw Swagger API docs.
2. **Resource Sizing**:
   - Recommended minimum: 2 vCPU and 2 GB RAM (e.g. AWS `t3.small` or `t4g.small`).
   - If running on a 1 GB instance (`t3.micro`), enable at least 2 GB of swap space (`sudo fallocate -l 2G /swapfile && sudo chmod 600 /swapfile && sudo mkswap /swapfile && sudo swapon /swapfile`) to prevent OOM during builds.
3. **Checking Status & Logs**:
   ```bash
   # In DinD mode:
   docker logs -f compilr

   # In Compose mode:
   docker compose logs -f
   docker compose ps
   ```
