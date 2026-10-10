# Compilr — Standalone Online Compiler & Execution Engine

[![Java](https://img.shields.io/badge/Java-25-orange?logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.x-brightgreen?logo=springboot)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-19-blue?logo=react)](https://react.dev/)
[![Docker](https://img.shields.io/badge/Docker-Sandboxed-2496ED?logo=docker)](https://www.docker.com/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

A high-performance, secure, multi-language online code compiler and execution environment built with **Spring Boot (Java 25)** and **React 19 (TypeScript, Vite, Monaco Editor, Tailwind CSS v4, shadcn/ui)**.

Compilr provides untrusted code execution within ephemeral, resource-constrained **Linux Isolate Sandboxes** inside **Docker containers**.

---

## Documentation Quick Links

* 📖 **[Configuration Guide (`docs/CONFIGURATION.md`)](docs/CONFIGURATION.md)** — Environment variables, resource limits, timeouts, abuse protection.
* 🚀 **[Deployment Guide (`docs/DEPLOYMENT.md`)](docs/DEPLOYMENT.md)** — How to deploy with DinD (All-In-One single container) or standard Docker Compose.
* 🛠️ **[Scripts Guide (`docs/SCRIPTS.md`)](docs/SCRIPTS.md)** — Automation scripts for building execution images and deploying.

---

## 1. What is Compilr?

Compilr solves the challenge of executing arbitrary, untrusted user code in real time while guaranteeing host safety, low execution latency, and deterministic resource capping.

Whether you are building a competitive programming platform, an educational coding playground, or an interactive technical interview tool, Compilr provides a complete, self-hosted execution backend and a polished web workbench.

### Key Capabilities
* **14 Supported Languages & Databases**: Out-of-the-box execution for compiled languages, interpreted scripting runtimes, and relational & document databases.
* **Dual-Layer Isolation**: Combines containerized environments (Docker) with multi-tenant Linux kernel sandboxing (**Isolate** using namespaces, cgroups, chroot, and RLIMITs).
* **Deterministic Resource Limits**: Strict enforcement of CPU time limits, wall-clock timeouts, memory caps, file-size limits, and process limits (`pidsLimit`).
* **Zero Host Contamination**: Untrusted user code never touches the host system; sandbox network access is disabled (`networkMode: none`) by default.
* **Sliding-Window Rate Limiting**: Redis-backed sliding-window rate limiter and per-environment job queues guard against API flooding and bot abuse.
* **Modern Web IDE**: Resizable Monaco Editor workbench with dark/light themes, keyboard shortcuts (`Cmd+Enter` / `Ctrl+Enter`), live stdout/stderr streams, compiler error diagnostics, telemetry metrics, and custom stdin input.

---

## 2. Architecture Overview

```text
[ Browser / Web Client ]
       │  (HTTP / JSON)
       ▼
[ Spring Boot REST API Gateway ]
       │
       ▼
[ ExecutionAdmissionService ] (Rate Limiting, Cache Check, Queue Admission)
       │
       ▼
[ CompilerService ] (Validation, Option Clamping, Telemetry Mapping)
       │
       ▼
[ CodeExecutionService ] (Orchestration & Container Lifecycle)
       │
       ├─────────────────────────────────┬────────────────────────────────┐
       ▼                                 ▼                                ▼
[ Compiled Languages ]          [ Interpreted Languages ]         [ Database Engines ]
(Java, C, C++, Rust, Go, Kotlin) (Python, Node.js, TypeScript)    (Postgres, MySQL, SQLite, Mongo)
       │                                 │                                │
       ▼                                 ▼                                │
[ Docker Container ]            [ Docker Container ]                      │
       │                                 │                                │
       ▼                                 ▼                                │
[ Isolate Sandbox Box ]         [ Isolate Sandbox Box ]                   ▼
(cgroups, RLIMIT, chroot)       (cgroups, RLIMIT, chroot)        [ Docker Container ]
                                                                 (HostConfig Bounds)
```

---

## 3. Supported Languages & Runtimes

| Language | Identifier | Version | Paradigm | Compiler / Runtime | Default File |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Java** | `java-21` | OpenJDK 21 | Compiled | `javac` & `java` | `Main.java` |
| **C** | `c-17` | C17 (GCC 14) | Compiled | `gcc -std=c17 -O2` | `main.c` |
| **C++** | `cpp-23` | C++23 (GCC 14) | Compiled | `g++ -std=c++23 -O2` | `main.cpp` |
| **Python** | `python-3.12` | CPython 3.12 | Interpreted | `python3` | `main.py` |
| **JavaScript** | `javascript-node-20` | Node.js 20 | Interpreted | `node` | `main.js` |
| **TypeScript** | `typescript-5.4` | TypeScript 5.4 | Interpreted | `ts-node` (Node.js 20) | `main.ts` |
| **Go** | `go-1.22` | Go 1.22 | Compiled | `go build` | `main.go` |
| **Rust** | `rust-1.75` | Rust 1.75 | Compiled | `rustc -O` | `main.rs` |
| **Kotlin** | `kotlin-1.9` | Kotlin 1.9 | Compiled | `kotlinc` & `java` | `Main.kt` |
| **PostgreSQL**| `postgresql-16` | PostgreSQL 16 | Database | `psql` | `main.sql` |
| **MySQL** | `mysql-8.0` | MySQL 8.0 | Database | `mysql` | `main.sql` |
| **SQLite** | `sqlite-3` | SQLite 3.45 | Database | `sqlite3` | `main.sql` |
| **MongoDB** | `mongodb-8.0` | MongoDB 8.0 | Database | `mongosh` | `main.js` |

---

## 4. REST API Summary

Interactive OpenAPI / Swagger UI is available at `/swagger-ui.html` on the server.

### Execute Code
**`POST /api/v1/compiler/execute`**

#### Example Request
```json
{
  "language": "python-3.12",
  "sourceCode": "import sys\nname = sys.stdin.read().strip()\nprint(f'Hello, {name}!')",
  "stdin": "World",
  "options": {
    "timeLimitMs": 5000,
    "memoryLimitKb": 262144
  }
}
```

#### Example Response (`200 OK`)
```json
{
  "executionId": "8f3b2169-dc30-4e31-893f-56bb4e0192e4",
  "language": "python-3.12",
  "status": "SUCCESS",
  "stdout": "Hello, World!\n",
  "stderr": "",
  "exitCode": 0,
  "executionTimeMs": 28,
  "memoryUsageKb": 12840,
  "logs": [
    "CPU Time: 0.02s",
    "Wall Time: 0.03s",
    "Memory: 12840 KB"
  ],
  "error": null
}
```

### List Languages
**`GET /api/v1/compiler/languages`** — Returns language metadata, syntax tags, and starter code boilerplate.

### Health Check
**`GET /api/v1/compiler/health`** — Returns service health status (`{"status":"UP"}`).

---

## 5. Security & Isolation Model

1. **Host Isolation**: All submitted code runs inside short-lived Docker containers.
2. **Linux Isolate Containerization**: Inside containers, code executes under the multi-tenant `isolate` binary:
   * **PID Isolation**: Process limit set to prevent fork bombs (`--processes=50`).
   * **Filesystem Chroot**: Process can only view mounted runtime libraries and its private `/box` directory.
   * **Resource Bounds**: Enforces `RLIMIT_CPU`, `RLIMIT_WALL`, and file creation size limits.
3. **Network Isolation**: Execution containers run with `--net=none` / `networkDisabled=true` to block SSRF, port scanning, and outbound data exfiltration.
4. **Deterministic Cleanup**: Container stop and delete routines are guaranteed in `finally` blocks, ensuring no orphaned processes or disk leaks even on client disconnection.

---

## 6. Project Layout

```text
compilr/
├── Dockerfile                      # All-in-One DinD container image
├── docker-compose.yml              # Multi-container production compose
├── .env.example                    # Universal configuration template
├── .env.dind.example               # DinD-specific configuration template
├── .env.compose.example            # Standard compose configuration template
├── docs/                           # Documentation
│   ├── CONFIGURATION.md            # Configuration guide & environment variables
│   ├── DEPLOYMENT.md               # Deployment guide (DinD & Standard Compose)
│   └── SCRIPTS.md                  # Script reference & automation usage
├── scripts/                        # Utility & deployment scripts
│   ├── build-execution-images.sh   # Builds 14 language sandbox images
│   ├── deploy.sh                   # Standard host deployment script
│   ├── run-dind.sh                 # Quick DinD runner script
│   └── entrypoint-dind.sh          # DinD container internal entrypoint
├── server/                         # Spring Boot 4 Backend (Java 25)
└── client/                         # React 19 Frontend (TypeScript, Monaco, Vite)
```

---

## 7. Next Steps

* To configure timeouts, memory limits, or rate limits, see the **[Configuration Guide](docs/CONFIGURATION.md)**.
* To deploy Compilr via Docker-in-Docker or Docker Compose, see the **[Deployment Guide](docs/DEPLOYMENT.md)**.
* To view operational scripts, see the **[Scripts Guide](docs/SCRIPTS.md)**.
