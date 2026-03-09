# Happy Ops Service

Production-grade AI Agent backend system using Kotlin and Ktor.

## Tech Stack

- **Kotlin** - Programming language
- **Ktor** - Web framework
- **Arrow** - Functional programming library
- **KMongo** - MongoDB driver with coroutines
- **Grok API** - LLM for agent intelligence

## Architecture

Clean architecture with the following layers:

```
com.example.aiorders
├── controller      # HTTP endpoints
├── service         # Business logic
├── agent           # AI agent orchestration
├── tools           # Tool implementations
├── repository      # Data access interfaces
├── repository.impl # Data access implementations
├── adapter         # External service adapters
├── memory          # Conversation memory
├── model           # Domain models
├── config          # Configuration
└── observability   # Logging and metrics
```

## Prerequisites

- JDK 17+
- MongoDB running on localhost:27017
- Grok API key

## Setup

1. Copy the config template:
```bash
cp src/main/resources/application.conf.template src/main/resources/application.conf
```

2. Add your Grok API key to `application.conf` or set as environment variable:
```bash
export GROK_API_KEY=your_api_key_here
```

## Build

```bash
./gradlew build
```

## Run

```bash
export GROK_API_KEY=your_api_key_here
./gradlew run
```

## Endpoints

- `GET /` - Service info
- `GET /health` - Health check
- `POST /chat` - Chat with AI agent
- `POST /orders` - Create order
- `GET /orders` - List orders
- `GET /orders/{id}` - Get order
- `PUT /orders/{id}` - Update order
- `DELETE /orders/{id}` - Delete order

## Configuration

Edit `src/main/resources/application.conf` to configure:
- Server port
- MongoDB connection
- Grok API settings
