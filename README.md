# Car Rental Service - Domain Driven Design with CQRS

Spring Boot application implementing sophisticated architectural patterns, including Domain-Driven Design (DDD), Command Query Responsibility Segregation (CQRS), and Event-Driven Architecture, specifically engineered for comprehensive vehicle rental domain management

## 🎯 Architecture Highlights

- **Domain-Driven Design** - Clean bounded contexts and rich domain models
- **CQRS Pattern** - Separate read and write models for optimal performance
- **Event-Driven Architecture** - Kafka for asynchronous event processing
- **Hexagonal Architecture** - Clear separation of domain, application, and infrastructure layers
- **Event Sourcing Ready** - Domain events published for all state changes

## 📖 Documentation

This project includes comprehensive documentation:

- **README.md** (this file) - Quick overview and getting started
- **[API_EXAMPLES.md](API_EXAMPLES.md)** - Complete API documentation with examples
- **[ARCHITECTURE.md](ARCHITECTURE.md)** - Architectural patterns and CQRS diagrams
- **[CONCEPTS.md](CONCEPTS.md)** - Deep dive into DDD building blocks
- **[PROJECT_SUMMARY.md](PROJECT_SUMMARY.md)** - Complete project summary

## 📁 Project Structure

The project follows **CQRS** and **Event-Driven Architecture** with separate read and write models:

```
src/main/java/com/rentalcar/
├── RentalCarApplication.java          # Main Spring Boot application
│
├── rental/                            # 🎯 RENTAL CONTEXT (Core Domain)
│   ├── domain/
│   │   ├── model/
│   │   │   ├── Rental.java            # Aggregate Root (Write Model)
│   │   │   ├── RentalPeriod.java      # Value Object
│   │   │   └── RentalStatus.java      # Enum
│   │   ├── repository/
│   │   │   └── RentalRepository.java  # Write Model Repository
│   │   ├── service/
│   │   │   └── RentalPricingService.java  # Domain service
│   │   └── event/                     # 📣 DOMAIN EVENTS
│   │       ├── RentalCreatedEvent.java
│   │       ├── RentalStartedEvent.java
│   │       ├── RentalCompletedEvent.java
│   │       └── RentalCancelledEvent.java
│   │
│   ├── command/                       # ✍️ WRITE SIDE (CQRS)
│   │   ├── api/
│   │   │   ├── CreateRentalCommand.java
│   │   │   ├── StartRentalCommand.java
│   │   │   ├── CompleteRentalCommand.java
│   │   │   └── CancelRentalCommand.java
│   │   └── handler/
│   │       └── RentalCommandHandler.java  # Executes commands, publishes events
│   │
│   ├── query/                         # 📖 READ SIDE (CQRS)
│   │   ├── model/
│   │   │   └── RentalView.java        # Denormalized Read Model (MongoDB)
│   │   ├── repository/
│   │   │   └── RentalViewRepository.java  # MongoDB Repository
│   │   ├── api/
│   │   │   ├── GetRentalQuery.java
│   │   │   ├── GetActiveRentalsQuery.java
│   │   │   └── GetCustomerRentalsQuery.java
│   │   └── handler/
│   │       └── RentalQueryHandler.java    # Handles queries from read model
│   │
│   └── infrastructure/
│       ├── persistence/
│       │   └── JpaRentalRepository.java   # H2 Write Model
│       ├── projection/
│       │   └── RentalProjectionUpdater.java  # 🔄 Kafka Consumer - Updates read models
│       └── web/
│           ├── RentalCommandController.java  # POST/PUT/DELETE endpoints
│           └── RentalQueryController.java    # GET endpoints
│
├── fleet/                             # 🚗 FLEET CONTEXT
├── customer/                          # 👤 CUSTOMER CONTEXT
│
└── shared/                            # 🔧 SHARED KERNEL
    ├── domain/
    │   └── Money.java
    ├── event/
    │   └── DomainEvent.java           # Base event interface
    └── messaging/
        ├── KafkaConfig.java           # Kafka configuration
        └── EventPublisher.java        # Publishes events to Kafka
```

## 🏗️ CQRS Architecture

This project implements a complete **CQRS (Command Query Responsibility Segregation)** pattern with separate write and read models.

### Architecture Overview

```
┌──────────────────────────────────────────────────────────────┐
│                       CLIENT LAYER                           │
│                  (REST API Consumers)                        │
└────────────┬─────────────────────────────┬───────────────────┘
             │                             │
    Commands (Write)                  Queries (Read)
    POST/PUT/DELETE                      GET
             │                             │
             ▼                             ▼
┌────────────────────────┐    ┌──────────────────────────┐
│   WRITE MODEL (H2)     │    │   READ MODEL (MongoDB)   │
│  - Normalized Schema   │    │  - Denormalized Views    │
│  - Business Logic      │    │  - Query Optimized       │
│  - Transaction ACID    │    │  - Fast Retrieval        │
└──────────┬─────────────┘    └───────────▲──────────────┘
           │                              │
           │ Domain Events                │
           └──────► KAFKA ────────────────┘
                   (Event Bus)

Response Times:
  • Commands: ~50-100ms (202 ACCEPTED)
  • Queries:  ~10-20ms  (200 OK)
```

### Key Components

**Write Side:**
- **Command Controllers** - Handle write operations (POST/PUT/DELETE)
- **Command Handlers** - Execute business logic and publish events
- **Aggregate Roots** - Enforce business rules (Rental, Car, Customer)
- **H2 Database** - Normalized relational storage
- **Event Publisher** - Publishes domain events to Kafka

**Read Side:**
- **Query Controllers** - Handle read operations (GET)
- **Query Handlers** - Retrieve data from read model
- **Projection Updaters** - Listen to events and update read model
- **MongoDB** - Denormalized document storage
- **Materialized Views** - Pre-computed, query-optimized data

**Event Bridge:**
- **Apache Kafka** - Message broker connecting both sides
- **Domain Events** - RentalCreated, RentalStarted, RentalCompleted, RentalCancelled

### 📊 Detailed CQRS Diagrams

For comprehensive architecture diagrams including:
- Complete CQRS implementation with all layers
- Detailed event flow (5-step process)
- Write side flow with timing breakdown
- Read side flow with projection updates
- Benefits and trade-offs analysis

**See: [ARCHITECTURE.md - CQRS Architecture Diagrams](ARCHITECTURE.md#cqrs-architecture-diagrams)**

## 🎯 Key Architecture Principles

### ✅ Clean Bounded Contexts
- Each context is completely independent
- No cross-context domain object references
- Contexts communicate via IDs only

### ✅ Hexagonal Architecture
- **Domain Layer**: Pure business logic (entities, value objects, domain services)
- **Application Layer**: Use case orchestration (application services)
- **Infrastructure Layer**: Technical concerns (JPA, REST controllers)

### ✅ DDD Building Blocks

**Aggregate Roots** (with unique identity):
- `Rental` - Core domain aggregate
- `Car` - Fleet aggregate
- `Customer` - Customer aggregate

**Value Objects** (immutable, no identity):
- `Money` - Shared across all contexts
- `RentalPeriod` - Rental duration
- `VehicleSpecification` - Car details
- `ContactInfo` - Customer contact
- `LicenseInfo` - License details

**Domain Services**:
- `RentalPricingService` - Complex pricing calculations with discounts

**Domain Events**:
- `RentalCreated` - Published when rental is created

## 🚀 Getting Started

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- Docker & Docker Compose

### Running the Application

1. **Start Infrastructure Services (Kafka, MongoDB, ZooKeeper)**
```bash
docker-compose up -d
```

This starts:
- **Kafka** on `localhost:9092` (Message broker)
- **ZooKeeper** on `localhost:2181` (Kafka coordination)
- **MongoDB** on `localhost:27017` (Read model database)

2. **Build the project:**
```bash
mvn clean install
```

3. **Run the application:**
```bash
mvn spring-boot:run
```

4. **Access Monitoring Tools:**

**H2 Console** (Write Model Database):
```
URL: http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:rentaldb
Username: sa
Password: (leave empty)
```

**MongoDB** (Read Model):
```
Connection: mongodb://admin:admin123@localhost:27017
Database: rental-read-db
Collection: rentalView (denormalized rental data)
```

### Stopping Services

```bash
# Stop all Docker services
docker-compose down

# Stop and remove volumes (clean slate)
docker-compose down -v
```

## 📝 API Examples

For complete API documentation with request/response examples, see **[API_EXAMPLES.md](API_EXAMPLES.md)**

The documentation includes:
- **Commands (Write Side)** - Rental, Fleet, and Customer Management operations
- **Queries (Read Side)** - Retrieve rentals, active rentals, customer history, available cars
- **Request/Response Examples** - All endpoints with sample JSON
- **Business Rules** - Validation rules for each operation
- **Error Responses** - Complete error handling documentation
- **Complete Workflow** - End-to-end testing scenarios
- **Performance Metrics** - Response times (~50-100ms for commands, ~10-20ms for queries)

## 🏗️ Architectural Highlights

### 1. CQRS Implementation

**Separate Models for Reading and Writing:**
- **Write Model**: H2 database with normalized tables, enforces business rules
- **Read Model**: MongoDB with denormalized documents, optimized for queries
- **Event Bridge**: Kafka keeps both sides synchronized

```java
// Write Side: Command Handler
@Transactional
public String handle(CreateRentalCommand command) {
    Rental rental = Rental.create(...);
    rentalRepository.save(rental);  // H2
    eventPublisher.publish(event);   // Kafka
    return rental.getRentalId();
}

// Read Side: Projection Updater
@KafkaListener(topics = "rental-events")
public void handleRentalEvent(RentalCreatedEvent event) {
    RentalView view = buildDenormalizedView(event);
    rentalViewRepository.save(view);  // MongoDB
}
```

### 2. Event-Driven Architecture

**All state changes publish domain events:**
- `RentalCreatedEvent` - When rental is reserved
- `RentalStartedEvent` - When car is picked up
- `RentalCompletedEvent` - When car is returned
- `RentalCancelledEvent` - When rental is cancelled

**Benefits:**
- Audit trail of all changes
- Asynchronous processing
- System decoupling
- Easy to add new consumers

### 3. DDD Tactical Patterns

**Aggregate Boundaries:**
Each aggregate has a clear boundary with one root entity. External access only through the root.

**Reference by ID:**
```java
// ❌ BAD - Direct object reference
private Customer customer;
private Car car;

// ✅ GOOD - Reference by ID only
private String customerId;
private String carId;
```

**Encapsulation & Invariants:**
All business rules enforced within the domain model:
```java
public void markAsRented() {
    if (this.availabilityStatus != AvailabilityStatus.AVAILABLE) {
        throw new IllegalStateException("Car is not available");
    }
    this.availabilityStatus = AvailabilityStatus.RENTED;
}
```

**Ubiquitous Language:**
Code uses business terminology: Rental, Fleet, Customer, Reserve, Pickup, Return

**Bounded Contexts:**
Three independent contexts with clear boundaries and responsibilities

### 4. Eventual Consistency

The read model updates asynchronously:
1. Command executes → Write model updated → Event published (milliseconds)
2. Event consumed → Read model updated (near real-time)
3. Query returns denormalized data from read model

This trade-off enables:
- Higher throughput
- Better scalability
- Optimized read performance

### Infrastructure
- **Message Broker**: Apache Kafka
- **Write Database**: H2 (in-memory)
- **Read Database**: MongoDB
- **Coordination**: ZooKeeper

## 🔍 Testing the Event Flow

1. **Create a rental** (Command API):
```bash
curl -X POST http://localhost:8080/api/commands/rentals -H "Content-Type: application/json" -d '{"customerId":"cust-123","carId":"car-456","startDate":"2025-11-01","endDate":"2025-11-05"}'
```

2. **Query the read model** (Query API):
```bash
curl http://localhost:8080/api/queries/rentals/{rental-id}
```
   - Returns denormalized data from MongoDB
   - Includes customer name, car details, pricing, status

3. **Check MongoDB**:
```javascript
// Connect to MongoDB
use rental-read-db

// View the denormalized rental document
db.rentalView.find().pretty()
```


