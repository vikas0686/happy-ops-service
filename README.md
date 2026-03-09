# Happy Ops Service

Production-grade AI Agent backend for order management using Kotlin and Ktor.

## 🚀 Features

- **AI-Powered Chat Interface** - Natural language order management
- **Tool Calling** - AI agent can execute order operations (create, list, search, stats)
- **RESTful API** - Full CRUD operations for orders
- **Conversation Memory** - Context-aware multi-turn conversations
- **Clean Architecture** - Layered design with separation of concerns

## 🛠 Tech Stack

- **Kotlin** - Modern JVM language
- **Ktor** - Async web framework
- **Arrow** - Functional programming (Either for error handling)
- **KMongo** - MongoDB driver with coroutines
- **Grok API** - LLM for agent intelligence
- **Kotlinx Serialization** - JSON handling

## 📋 Prerequisites

- JDK 17+
- MongoDB (running on localhost:27017)
- Grok API key ([Get one here](https://console.groq.com))

## ⚙️ Setup

1. **Clone the repository**
```bash
git clone <your-repo-url>
cd happy-ops-service
```

2. **Install MongoDB** (if not already installed)
```bash
# macOS
brew tap mongodb/brew
brew install mongodb-community@8.0
brew services start mongodb/brew/mongodb-community@8.0

# Verify MongoDB is running
mongosh --eval "db.adminCommand('ping')"
```

3. **Configure the application**
```bash
# Copy the template
cp src/main/resources/application.conf.template src/main/resources/application.conf

# Edit and add your Grok API key
# Or set as environment variable
export GROK_API_KEY=your_api_key_here
```

4. **Build the project**
```bash
./gradlew build
```

5. **Run the application**
```bash
./gradlew run
```

The service will start on `http://localhost:8080`

## 📊 Seed Sample Data

Load 200 realistic orders into the database:

```bash
./scripts/seed_orders.sh
```

This creates orders with:
- 8 different customers
- Mix of products (electronics, accessories)
- Random quantities and amounts
- Indian addresses and phone numbers

## 🔌 API Endpoints

### Health & Info
- `GET /` - Service info
- `GET /health` - Health check
- `GET /metrics` - Tool execution metrics

### Chat (AI Agent)
- `POST /chat` - Chat with AI agent
  ```json
  {
    "message": "list orders from Vikas",
    "sessionId": "optional-session-id"
  }
  ```
- `DELETE /chat/session/{sessionId}` - Clear conversation history

### Orders (REST API)
- `POST /orders` - Create order
- `GET /orders` - List all orders
- `GET /orders/{id}` - Get order by ID
- `PUT /orders/{id}` - Update order
- `DELETE /orders/{id}` - Delete order

## 💬 Example Chat Queries

Try these with the AI agent:

```bash
# List orders
curl -X POST http://localhost:8080/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "show me recent orders"}'

# Search by customer
curl -X POST http://localhost:8080/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "any orders from Vikas?"}'

# Get statistics
curl -X POST http://localhost:8080/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "what are the order stats for Priya?"}'

# Create order
curl -X POST http://localhost:8080/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "create an order for merchant M001 with amount 5000"}'
```

## 🏗 Architecture

```
com.happyops
├── controller      # HTTP endpoints & routing
├── service         # Business logic
├── agent           # AI agent orchestration
│   ├── AgentPlanner      # LLM interaction
│   ├── AgentExecutor     # Tool execution
│   └── AgentOrchestrator # Workflow coordination
├── tools           # Tool implementations (create, list, stats)
├── repository      # Data access layer
├── adapter         # External services (Grok, MongoDB)
├── memory          # Conversation memory management
├── model           # Domain models & DTOs
├── config          # Configuration
└── observability   # Logging & metrics
```

## 🔧 Configuration

Edit `src/main/resources/application.conf`:

```hocon
ktor {
    deployment {
        port = 8080
    }
}

mongo {
    uri = "mongodb://localhost:27017"
    database = "aiorders"
}

grok {
    apiKey = ""  # Your API key
    baseUrl = "https://api.groq.com/openai/v1"
    model = "llama3-groq-70b-8192-tool-use-preview"
}
```

## 🧪 Available AI Tools

The agent has access to these tools:

1. **create_order** - Create new orders
2. **list_orders** - List recent orders (max 10, filterable by customer)
3. **get_order** - Get order details by ID
4. **order_stats** - Get order statistics (count, total amount, status breakdown)

## 📝 Order Model

```kotlin
Order {
    id: String
    displayOrderId: String
    orderId: String
    orderType: ONLINE | OFFLINE | SUBSCRIPTION
    orderDate: Instant
    merchantOrderReference: String
    orderAmount: { value: Double, currency: String }
    orderStatus: OPEN | CONFIRMED | PROCESSING | SHIPPED | 
                 DELIVERED | CANCELLED | REFUNDED | FAILED | CLOSED
    merchantId: String
    channel: WEB | MOBILE | API | POS
    customer: { name, email, phone, address }
    orderDetails: { items, metadata }
}
```

## 🐛 Troubleshooting

**MongoDB connection failed**
```bash
# Check if MongoDB is running
brew services list | grep mongodb

# Start MongoDB
brew services start mongodb/brew/mongodb-community@8.0
```

**Grok API rate limit**
- Free tier has daily token limits
- Switch to a smaller model or upgrade your plan
- Use `llama-3.1-8b-instant` for higher rate limits

**Build fails**
```bash
# Clean and rebuild
./gradlew clean build
```

## 📚 Development

**Run tests**
```bash
./gradlew test
```

**Format code**
```bash
./gradlew ktlintFormat
```

**Check dependencies**
```bash
./gradlew dependencies
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License.

## 🙏 Acknowledgments

- [Ktor](https://ktor.io/) - Async web framework
- [Arrow](https://arrow-kt.io/) - Functional programming
- [Groq](https://groq.com/) - Fast LLM inference
- [KMongo](https://litote.org/kmongo/) - MongoDB Kotlin driver
