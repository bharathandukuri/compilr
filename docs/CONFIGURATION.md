# Compilr — Configuration Guide

Compilr is designed to run with **zero required configuration** out of the box. Safe, production-ready defaults are built directly into the server application.

If you wish to customize execution timeouts, memory limits, abuse rate limiting, or logging levels, you can create a `.env` file in the project root.

---

## Configuration Files Overview

| File | Purpose |
| :--- | :--- |
| **[`.env.example`](../.env.example)** | Default template containing all optional configuration settings. |
| **[`.env.dind.example`](../.env.dind.example)** | Tailored for **Docker-in-Docker (All-in-One)** single-container deployment. |
| **[`.env.compose.example`](../.env.compose.example)** | Tailored for **Standard Docker Compose** multi-container deployment (includes optional host port mapping overrides). |

To apply your settings:
```bash
cp .env.example .env
# Edit the settings you want to change:
nano .env
```

---

## Detailed Settings Reference

### 1. Code & Input Size Limits

These settings protect the backend against oversized payloads and Denial-of-Service attacks.

| Variable | Description | Default | Unit |
| :--- | :--- | :--- | :--- |
| `MAX_SOURCE_SIZE_BYTES` | Maximum allowed source code size per execution. Code larger than this is rejected with a validation error. | `262144` | Bytes (256 KB) |
| `MAX_STDIN_SIZE_BYTES` | Maximum allowed standard input (`stdin`) sent to the program. | `262144` | Bytes (256 KB) |
| `MAX_OUTPUT_SIZE_BYTES` | Maximum combined standard output (`stdout` + `stderr`) captured. Prevents infinite print loops from filling system memory. | `1048576` | Bytes (1 MB) |
| `DEFAULT_MEMORY_LIMIT_KB` | Default RAM memory cap for sandbox execution containers. | `262144` | KB (256 MB) |

---

### 2. Execution Time Limits

These settings prevent runaway programs, infinite recursion, or slow code from tying up server resources.

| Variable | Description | Default | Unit |
| :--- | :--- | :--- | :--- |
| `DEFAULT_TIMEOUT_MS` | Default execution wall-clock time limit. If program execution exceeds this, it is terminated with `TIME_LIMIT_EXCEEDED`. | `5000` | Milliseconds (5s) |
| `MAX_TIMEOUT_MS` | Maximum execution time limit a client is permitted to request. Requests asking for higher timeouts are clamped to this maximum. | `15000` | Milliseconds (15s) |
| `MIN_TIMEOUT_MS` | Minimum execution time limit permitted. | `100` | Milliseconds (0.1s) |

---

### 3. Rate Limiting (Abuse & Bot Protection)

Compilr uses a sliding-window rate limiter powered by Redis to guard against API flooding and automated bot attacks.

| Variable | Description | Default | Options |
| :--- | :--- | :--- | :--- |
| `RATE_LIMIT_ENABLED` | Enables or disables sliding-window rate limiting. | `true` | `true`, `false` |
| `RATE_LIMIT_MAX_REQUESTS` | Maximum number of compilation/execution requests allowed per unique client in the window. | `15` | Integer |
| `RATE_LIMIT_WINDOW_SECONDS`| Time window duration for rate limiting. (e.g. 15 requests allowed every 60 seconds). | `60` | Seconds |

When a client exceeds this limit, the server responds with HTTP `429 Too Many Requests` along with a `Retry-After` header.

---

### 4. Concurrency & Queue Protection

Compilr queues incoming execution tasks per environment and across the whole server to prevent CPU saturation.

| Variable | Description | Default | Recommendation |
| :--- | :--- | :--- | :--- |
| `MAX_CONCURRENT_JOBS` | Global limit on the number of execution sandbox containers running simultaneously across all languages. | `12` | Set to 8–12 on 1–2 vCPU instances (e.g. AWS `t3.small`); 16–32 on 4+ vCPU instances. |
| `MAX_QUEUE_WAIT_TIMEOUT_MS` | Maximum time a request will wait in the queue for an execution slot before failing with a timeout error. | `10000` (10s) | Milliseconds |

---

### 5. Server Logging

| Variable | Description | Default | Options |
| :--- | :--- | :--- | :--- |
| `LOG_LEVEL` | Logging verbosity for the Compilr application. | `INFO` | `INFO`, `WARN`, `ERROR`, `DEBUG` |

---

### 6. Host Port Overrides (Standard Compose Mode Only)

In standard Docker Compose mode, you can customize the host ports mapped to each service:

| Variable | Description | Default Port |
| :--- | :--- | :--- |
| `CLIENT_PORT` | Host port mapped to the React frontend UI. | `6990` |
| `SERVER_PORT` | Host port mapped to the Spring Boot REST API & Swagger UI. | `6991` |
| `REDIS_PORT` | Host port mapped to Redis. | `6992` |

*(In DinD mode, the host port is mapped directly via `docker run -p <host-port>:6990`).*
