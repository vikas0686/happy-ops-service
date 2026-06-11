# happy-ops-service

> A reference implementation of an LLM-powered AI agent with tool-calling, built with Kotlin and Ktor.

![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)
![Kotlin](https://img.shields.io/badge/Kotlin-1.9-blue.svg)
![Ktor](https://img.shields.io/badge/Ktor-2.3-orange.svg)

---

## What is this?

Most AI agent examples are in Python. This project shows how to build a **production-style AI agent** in Kotlin using Ktor — with real tool-calling, conversation memory, and a proper layered architecture.

The domain is order management: the agent answers natural language questions about orders by deciding which tools to call, executing them against MongoDB, and returning a coherent response.

## The Problem it Solves

Kotlin/JVM developers learning AI integration face two friction points:

1. **No idiomatic reference** — LLM/agent examples are almost exclusively Python; translating patterns to Kotlin is non-trivial.
2. **Toy examples don't teach production patterns** — most demos skip error handling, memory, observability, and clean architecture.

This repo is a working, domain-complete reference that Kotlin developers can read, fork, and adapt.

---

## Architecture

```
┌─────────────────┐        POST /chat
│  happy-ops-     │ ──────────────────────────────────────────┐
│  portal (React) │                                           ▼
└─────────────────┘                              ┌─────────────────────┐
                                                 │   ChatController    │
                                                 └────────┬────────────┘
                                                          │
                                                 ┌────────▼────────────┐
                                                 │  AgentOrchestrator  │
                                                 └──┬──────────────┬───┘
                                                    │              │
                              ┌─────────────────────▼──┐   ┌───────▼──────────────┐
                              │     AgentPlanner        │   │   ConversationMemory  │
                              │  (Groq LLM via HTTP)    │   │   (in-memory/session) │
                              └─────────────────────────┘   └──────────────────────┘
                                                    │
                                         ┌──────────▼──────────┐
                                         │   AgentExecutor     │
                                         └──────────┬──────────┘
                                                    │
                        ┌───────────────────────────┼───────────────────────────┐
                        ▼                           ▼                           ▼
               ┌─────────────────┐      ┌──────────────────┐      ┌─────────────────────┐
               │  ListOrdersTool │      │  CreateOrderTool │      │  OrderStatsTool /   │
               │  GetOrderTool   │      │                  │      │  (extensible)       │
               └────────┬────────┘      └────────┬─────────┘      └──────────┬──────────┘
                        └───────────────────────┬─┘                          │
                                                ▼                            │
                                    ┌───────────────────────┐                │
                                    │   OrderRepository     │◄───────────────┘
                                    └───────────┬───────────┘
                                                ▼
                                         ┌─────────────┐
                                         │   MongoDB   │
                                         └─────────────┘
```

## How the Agent Loop Works

```
User message
     │
     ▼
AgentPlanner sends message + conversation history + tool definitions to Groq LLM
     │
     ▼
LLM responds with either:
  ├─ tool_call  → AgentExecutor runs the tool, result is added to conversation
  │               └─ loop back to AgentPlanner with the tool result
  └─ text       → Final answer returned to the user
```

Each session maintains its own conversation history in memory, so the agent understands follow-up questions like *"what about the second one?"*.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin 1.9 |
| Web framework | Ktor 2.3 (async, coroutine-native) |
| LLM | Groq API — `llama3-groq-70b-8192-tool-use-preview` |
| Database | MongoDB via KMongo (coroutine) |
| Error handling | Arrow (`Either`) |
| Serialization | Kotlinx Serialization |
| Logging | Logback + kotlin-logging |

---

## Prerequisites

- JDK 17+
- MongoDB running on `localhost:27017`
- A free [Groq API key](https://console.groq.com)

---

## Setup

```bash
git clone https://github.com/your-org/happy-ops-service.git
cd happy-ops-service
```

**Install MongoDB (macOS):**
```bash
brew tap mongodb/brew && brew install mongodb-community@8.0
brew services start mongodb/brew/mongodb-community@8.0
```

**Configure the app:**
```bash
cp src/main/resources/application.conf.template src/main/resources/application.conf
# Edit application.conf and set your Groq API key
# OR use an environment variable:
export GROK_API_KEY=your_api_key_here
```

**Run:**
```bash
./gradlew run
# Service starts on http://localhost:8080
```

**Seed sample data (200 orders):**
```bash
./scripts/seed_orders.sh
```

---

## Configuration

`src/main/resources/application.conf`:

```hocon
ktor.deployment.port = 8080

mongo {
    uri = "mongodb://localhost:27017"
    database = "aiorders"
}

grok {
    apiKey = ""          # or set GROK_API_KEY env var
    baseUrl = "https://api.groq.com/openai/v1"
    model = "llama-3.1-8b-instant"
}
```

---

## API Reference

### Chat (AI Agent)
| Method | Path | Description |
|---|---|---|
| `POST` | `/chat` | Send a message to the agent |
| `DELETE` | `/chat/session/{sessionId}` | Clear conversation history |

```bash
curl -X POST http://localhost:8080/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "show me recent orders from Vikas", "sessionId": "optional-id"}'
```

### Orders (REST)
| Method | Path | Description |
|---|---|---|
| `POST` | `/orders` | Create order |
| `GET` | `/orders` | List all orders |
| `GET` | `/orders/{id}` | Get by ID |
| `PUT` | `/orders/{id}` | Update |
| `DELETE` | `/orders/{id}` | Delete |

### Health
| Method | Path | Description |
|---|---|---|
| `GET` | `/health` | Health check |
| `GET` | `/metrics` | Tool execution metrics |

---

## Available Agent Tools

| Tool | Description |
|---|---|
| `list_orders` | List recent orders, filterable by customer (max 10) |
| `get_order` | Fetch a single order by ID |
| `create_order` | Create a new order |
| `order_stats` | Count, total amount, status breakdown per customer |

Adding a new tool: implement the `Tool` interface and register it in `ToolRegistry`. The agent picks it up automatically.

---

## Example Queries

```bash
# Natural language — agent decides which tool to use
curl -X POST http://localhost:8080/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "any orders from Priya this week?"}'

curl -X POST http://localhost:8080/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "what is the total order value for merchant M001?"}'

curl -X POST http://localhost:8080/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "create an order for merchant M002, amount 3500"}'
```

---

## Project Structure

```
src/main/kotlin/com/happyops/
├── Application.kt          # Entry point, DI wiring
├── agent/
│   ├── AgentOrchestrator   # Coordinates the full agent loop
│   ├── AgentPlanner        # Talks to the LLM (Groq)
│   └── AgentExecutor       # Resolves and runs tool calls
├── tools/
│   ├── Tool.kt             # Tool interface
│   ├── ToolRegistry.kt     # Registers available tools
│   ├── ListOrdersTool.kt
│   ├── GetOrderTool.kt
│   ├── CreateOrderTool.kt
│   └── OrderStatsTool.kt
├── memory/
│   ├── ConversationMemory  # Per-session message history
│   └── MemoryStore         # In-memory store (sessionId → history)
├── controller/             # Ktor routes (Chat, Order)
├── service/                # Business logic
├── repository/             # MongoDB data access
│   └── impl/               # Repository implementations
├── adapter/                # External clients (Groq, MongoDB)
├── model/                  # Domain models & DTOs
├── config/                 # App configuration
└── observability/          # Logging & tool metrics
```

---

## Troubleshooting

**MongoDB not connecting:**
```bash
brew services list | grep mongodb
brew services start mongodb/brew/mongodb-community@8.0
```

**Groq rate limit hit:**  
Switch to `llama-3.1-8b-instant` in `application.conf` for higher free-tier limits.

**Build fails:**
```bash
./gradlew clean build
```

---

## Contributing

1. Fork the repo and create a feature branch
2. Keep changes focused — one concern per PR
3. For new tools: implement `Tool`, register in `ToolRegistry`, add a test
4. Open a PR with a clear description of what and why

---

## License

MIT — see [LICENSE](LICENSE)
