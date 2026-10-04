# Compilr — Standalone Online Compiler & Execution Engine

A high-performance, secure, production-ready online code compiler and execution environment built with **Spring Boot 4 (Java 25)** and **React 19 (TypeScript, Vite, Monaco Editor, Tailwind CSS v4, shadcn/ui)**.

Compilr provides untrusted code execution within ephemeral, resource-constrained **Linux Isolate Sandboxes** inside **Docker containers**.

---

## 1. Overview

Compilr solves the challenge of executing arbitrary, untrusted user code in real time while guaranteeing host safety, low execution latency, and deterministic resource capping.

### Key Capabilities
* **Multi-Language Execution**: Out-of-the-box support for Java 21, Python 3.12, C 17, C++ 23, Node.js 20, MySQL 8.0, and PostgreSQL 16.
* **Dual Isolation Layer**: Combines containerized environments (Docker) with multi-tenant Linux kernel sandboxing (**Isolate** using namespaces, cgroups, chroot, and RLIMITs).
* **Multi-tenant & Concurrent**: Each execution is isolated with unique execution IDs, separate filesystem boxes, and guaranteed lifecycle cleanup.
* **Deterministic Resource Limits**: Configurable CPU time limits, wall-clock timeouts, memory caps, file-size limits, and process limits (`pidsLimit`).
* **Zero Host Contamination**: Untrusted code never executes directly on the host machine; network access is disabled (`--net=none`) and containers run with read-only root filesystems where applicable.
* **Rich Web-based IDE**: Resizable Monaco Editor workbench with dark/light themes, keyboard shortcuts (`Cmd+Enter` / `Ctrl+Enter`), live stdout/stderr streams, compiler error diagnostics, telemetry metrics, and custom stdin input.

---

## 2. Architecture

```text
[ Browser / Web Client ]
       │  (HTTP / JSON)
       ▼
[ Spring Boot 4 REST API Gateway ]
       │
       ▼
[ CompilerService ] (Validation, Option Clamping, Telemetry Mapping)
       │
       ▼
[ CodeExecutionService ] (Orchestration & File Management)
       │
       ├─────────────────────────────────┬────────────────────────────────┐
       ▼                                 ▼                                ▼
[ Compiled Language ]          [ Interpreted Language ]         [ Database Query ]
(Java 21, C 17, C++ 23)        (Python 3.12, Node.js 20)        (PostgreSQL 16, MySQL 8.0)
       │                                 │                                │
       ▼                                 ▼                                │
[ Docker Container ]            [ Docker Container ]                      │
       │                                 │                                │
       ▼                                 ▼                                │
[ Isolate Sandbox Box ]         [ Isolate Sandbox Box ]                   ▼
(cgroups, RLIMIT, chroot)       (cgroups, RLIMIT, chroot)        [ Docker Container ]
                                                                 (HostConfig Limits)
```

---

## 3. Supported Languages & Environments

| Language | Identifier | Version | Paradigm | Compiler / Runtime | Default File |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Java** | `java-21` (alias: `java`) | OpenJDK 21 | Compiled | `javac` & `java` | `Main.java` |
| **C** | `c-17` (alias: `c`) | C17 (GCC 14) | Compiled | `gcc -std=c17 -O2` | `main.c` |
| **C++** | `cpp-23` (alias: `cpp`, `c++`) | C++23 (GCC 14) | Compiled | `g++ -std=c++23 -O2` | `main.cpp` |
| **Python** | `python-3.12` (alias: `python`, `py`) | CPython 3.12 | Interpreted | `python3` | `main.py` |
| **JavaScript** | `javascript-node-20` (alias: `js`, `node`) | Node.js 20 | Interpreted | `node` | `main.js` |
| **TypeScript** | `typescript-5.4` (alias: `ts`) | TypeScript 5.4 | Interpreted | `ts-node` (Node.js 20) | `main.ts` |
| **Go** | `go-1.22` (alias: `go`, `golang`) | Go 1.22 | Compiled | `go build` | `main.go` |
| **Rust** | `rust-1.75` (alias: `rust`, `rs`) | Rust 1.75 | Compiled | `rustc -O` | `main.rs` |
| **C#** | `csharp-12` (alias: `c#`, `dotnet`) | .NET 8.0 | Compiled | `dotnet run` | `Program.cs` |
| **Kotlin** | `kotlin-1.9` (alias: `kt`) | Kotlin 1.9 | Compiled | `kotlinc` & `java` | `Main.kt` |
| **Dart** | `dart-3.4` (alias: `dart`) | Dart 3.4 | Interpreted | `dart run` | `main.dart` |
| **PHP** | `php-8.3` (alias: `php`) | PHP 8.3 | Interpreted | `php` | `main.php` |
| **PostgreSQL**| `postgresql-16` (alias: `postgres`, `psql`) | PostgreSQL 16 | Database | `psql` | `main.sql` |
| **MySQL** | `mysql-8.0` (alias: `mysql`, `sql`) | MySQL 8.0 | Database | `mysql` | `main.sql` |
| **SQLite** | `sqlite-3` (alias: `sqlite`, `sqlite3`) | SQLite 3.45 | Database | `sqlite3` | `main.sql` |
| **MongoDB** | `mongodb-8.0` (alias: `mongo`, `nosql`) | MongoDB 8.0 | Database | `mongosh` | `main.js` |

---

## 4. REST API Specification

### 4.1 Execute Code
**`POST /api/v1/compiler/execute`**

#### Request Payload
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

#### Successful Response (`200 OK`)
```json
{
  "executionId": "8f3b2169-dc30-4e31-893f-56bb4e0192e4",
  "language": "python-3.12",
  "status": "SUCCESS",
  "stdout": "Hello, World!\n",
  "stderr": "",
  "exitCode": 0,
  "exitSignal": 0,
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

#### Compilation Failure Response (`200 OK`)
```json
{
  "executionId": "b18b965e-26f5-42df-bb51-d922bb33f990",
  "language": "cpp-23",
  "status": "COMPILATION_ERROR",
  "stdout": "",
  "stderr": "main.cpp: In function 'int main()':\nmain.cpp:3:5: error: 'syntax_error' was not declared in this scope\n",
  "exitCode": 1,
  "exitSignal": null,
  "executionTimeMs": 1420,
  "memoryUsageKb": null,
  "logs": [
    "Compilation failed with exit code: 1"
  ],
  "error": null
}
```

### 4.2 List Supported Languages
**`GET /api/v1/compiler/languages`**

Returns array of supported language configurations, starter boilerplate templates, and aliases.

### 4.3 Health Check
**`GET /api/v1/compiler/health`**

Returns `{ "status": "UP", "service": "compilr" }`.

### 4.4 Swagger / OpenAPI Documentation
Interactive Swagger UI is accessible at:
```text
http://localhost:8080/swagger-ui.html
```

---

## 5. Security Model & Sandboxing

1. **Host Isolation**: All submitted code runs inside short-lived Docker containers.
2. **Linux Isolate Containerization**: Inside containers, code executes under the multi-tenant `isolate` binary:
   * **PID Isolation**: Process limit set to prevent fork bombs (`--processes=50`).
   * **Filesystem Chroot**: Process can only view mounted runtime libraries and its private `/box` directory.
   * **Resource Bounds**: Enforces `RLIMIT_CPU`, `RLIMIT_WALL`, and file creation size limits.
   * **Address Space Management**: VM runtimes (Java HotSpot and Node.js V8) are exempted from virtual address space starvation (`--mem`) while C/C++/Python enforce strict resident memory limits.
3. **Network Isolation**: Execution containers run with `--net=none` / `networkDisabled=true` to block SSRF, port scanning, and outbound data exfiltration.
4. **Input & Payload Caps**:
   * Maximum Source Code Size: `256 KB`
   * Maximum Standard Input Size: `256 KB`
   * Output Truncation: Hard cap at `1 MB` prevents buffer exhaustion attacks from infinite `while(1) printf(...)`.
5. **Deterministic Cleanup**: Container stop and delete routines are guaranteed in `finally` blocks, ensuring no orphaned processes or disk leaks even on client disconnection.

---

## 6. Project Directory Structure

```text
compilr/
├── docker-compose.yml              # Production Compose orchestration
├── .env.example                    # Environment variable template
├── server/                         # Spring Boot 4 Backend
│   ├── pom.xml                     # Maven build descriptor (Java 25)
│   ├── Dockerfile                  # Multi-stage production container
│   ├── src/main/java/com/bharathandukuri/compilr/
│   │   ├── compiler/               # Online Compiler Domain
│   │   │   ├── controller/         # CompilerController, LanguageController
│   │   │   ├── service/            # CompilerService & CompilerServiceImpl
│   │   │   ├── dto/                # ExecuteRequest, ExecuteResponse, LanguageResponse
│   │   │   ├── enums/              # ExecutionStatus
│   │   │   ├── exception/          # GlobalExceptionHandler, Custom Exceptions
│   │   │   └── configuration/      # CompilerProperties, CorsConfig, OpenApiConfig
│   │   ├── execution/              # Core Sandbox Execution Subsystem
│   │   │   ├── service/            # CodeExecutionService, DockerExecutionService, IsolateExecutionService
│   │   │   ├── config/             # DockerConfig, DockerProperties
│   │   │   ├── registry/           # DockerImageRegistry
│   │   │   └── dto/                # Container details, constraints, results
│   │   └── language/               # Language Runtime Registry & Factories
│   └── src/main/resources/
│       ├── application.properties  # Server configuration
│       └── docker/                 # Language Dockerfile templates
└── client/                         # React 19 Frontend
    ├── package.json
    ├── vite.config.ts
    ├── Dockerfile                  # Multi-stage container
    ├── nginx.conf                  # Production reverse-proxy
    └── src/
        ├── api/                    # Axios API client & compiler endpoints
        ├── components/
        │   ├── ide/                # Header, CodeEditor, StdinPanel, OutputPanel
        │   ├── ui/                 # shadcn/ui component library
        │   └── theme-provider.tsx  # Dark/Light theme manager
        ├── hooks/                  # useCompiler state hook
        ├── types/                  # TypeScript interface contracts
        └── utils/                  # LocalStorage state persistence
```

---

## 7. Local Development Setup

### Prerequisites
* **Java 25** (or compatible JDK 21+)
* **Node.js 20+** and `npm`
* **Docker Daemon** running locally (`/var/run/docker.sock`)

### 1. Start Backend Server
```bash
cd server
./mvnw spring-boot:run
```
The server will start at `http://localhost:8080`.
Verify health:
```bash
curl http://localhost:8080/api/v1/compiler/health
```

### 2. Start Frontend Web Client
```bash
cd client
npm install
npm run dev
```
Open `http://localhost:5173` in your browser. The Vite dev server will proxy `/api` calls directly to the backend.

---

## 8. Production Deployment with Docker Compose (VPS Ready)

### 8.1 Exposed Ports Architecture
| Service | Container | Host Exposed Port | URL / Access | Description |
| :--- | :--- | :--- | :--- | :--- |
| **Client** | `compilr-client` | **`6990`** | `http://<vps-ip>:6990` | Production React SPA served via optimized Nginx |
| **Server** | `compilr-server` | **`6991`** | `http://<vps-ip>:6991` | Spring Boot 4 REST API, Health & Swagger Docs |
| **Redis** | `compilr-redis` | **`6992`** | `localhost:6992` | Redis 7 for rate-limiting, job queueing & token cache |

### 8.2 Security: Network Isolation Guarantees
* **No Default Bridge for Sandboxes**: User-submitted code execution containers **never** use the default Docker `bridge` network (`docker0`).
* **Complete Network Denial (`networkMode: none`)**: By default, all compiler sandboxes and database evaluation containers run with `--net=none` and `networkDisabled=true`. They possess no external network interfaces (only loopback `127.0.0.1`).
* **Isolated Internal Sandbox Network**: If networking is ever required, containers attach to `compilr-sandbox-net` (created with `--internal`), which has no default gateway to the host and cannot communicate with the host's exposed ports (`6990`, `6991`, `6992`).
* **Application Network Isolation**: The client, server, and Redis communicate via a dedicated user-defined network (`compilr-app-net`).

### 8.3 One-Command VPS Deployment
Run the automated deployment script:
```bash
chmod +x scripts/deploy.sh
./scripts/deploy.sh
```

### 8.4 Manual Deployment Step-by-Step

#### 1. Configure Environment
```bash
cp .env.example .env
# Edit .env if you wish to customize passwords or limits:
nano .env
```

#### 2. Build Sandbox Execution Images (All 17 Environments)
```bash
chmod +x scripts/build-execution-images.sh
./scripts/build-execution-images.sh
```
Verify images:
```bash
docker images 'execution/*'
```

#### 3. Start Production Services
```bash
docker compose up -d --build
```

#### 4. Verify Service Health
```bash
docker compose ps
curl http://localhost:6991/api/v1/compiler/health
```

#### 5. View Logs or Stop Stack
```bash
# Stream live logs
docker compose logs -f

# View specific service logs
docker compose logs -f server

# Stop all services safely
docker compose down
```

---

## 9. Running Tests

### Run Backend Unit and Live Integration Tests:
```bash
cd server
./mvnw test
```
Executes:
* Compiler service options and size boundary validation tests
* Language registry and alias resolution tests
* MockMvc controller response tests
* Live Docker execution tests across Java 21, Python 3.12, and C++ 23, verifying compilation error capture, runtime exception handling, infinite loop timeout enforcement, and concurrent executions.

### Run Client Typecheck & Build:
```bash
cd client
npm run build
```
Verifies zero TypeScript errors and compiles the production bundle.
