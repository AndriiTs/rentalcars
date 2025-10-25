# Domain Driven Design Architecture - Car Rental Service

## Bounded Contexts and Aggregates

```
┌─────────────────────────────────────────────────────────────────────┐
│                     CAR RENTAL DOMAIN                               │
│                                                                     │
│  ┌────────────────────┐  ┌──────────────────┐  ┌─────────────────┐  │
│  │  RENTAL MANAGEMENT │  │ FLEET MANAGEMENT │  │    CUSTOMER     │  │
│  │   (Core Domain)    │  │   (Supporting)   │  │  MANAGEMENT     │  │
│  │                    │  │                  │  │  (Supporting)   │  │
│  └────────────────────┘  └──────────────────┘  └─────────────────┘  │
│           │                      │                      │           │
│           │                      │                      │           │
│  ┌────────▼────────┐    ┌───────▼────────┐   ┌────────▼─────────┐   │
│  │  Rental         │    │  Car           │   │  Customer        │   │
│  │  [Aggregate]    │◄───┤  [Aggregate]   │   │  [Aggregate]     │   │
│  │                 │    │                │   │                  │   │
│  │  - rentalId     │    │  - carId       │   │  - customerId    │   │
│  │  - customerId   │    │  - vin         │   │  - firstName     │   │
│  │  - carId        │    │  - licensePlate│   │  - lastName      │   │
│  │  - rentalPeriod │    │  - specification   │  - contactInfo   │   │
│  │  - totalCost    │    │  - dailyRate   │   │  - licenseInfo   │   │
│  │  - status       │    │  - status      │   │  - verified      │   │
│  └─────────────────┘    └────────────────┘   └──────────────────┘   │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘

              ▲                    ▲                    ▲
              │                    │                    │
              │  Reference by ID   │  Reference by ID   │
              │  (NOT direct obj)  │  (NOT direct obj)  │
              └────────────────────┴────────────────────┘
```

## Layered Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    INFRASTRUCTURE LAYER                     │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐   │
│  │ REST APIs    │  │ JPA Repos    │  │ Event Publishers │   │
│  └──────────────┘  └──────────────┘  └──────────────────┘   │
└───────────────────────────────┬─────────────────────────────┘
                                │
┌───────────────────────────────▼─────────────────────────────┐
│                    APPLICATION LAYER                        │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐   │
│  │ RentalService│  │ FleetService │  │ CustomerService  │   │
│  │ (Use Cases)  │  │ (Use Cases)  │  │ (Use Cases)      │   │
│  └──────────────┘  └──────────────┘  └──────────────────┘   │
└───────────────────────────────┬─────────────────────────────┘
                                │
┌───────────────────────────────▼─────────────────────────────┐
│                       DOMAIN LAYER                          │
│  ┌─────────────────────────────────────────────────────┐    │
│  │              ENTITIES & AGGREGATES                  │    │
│  │  Rental (Root) │ Car (Root) │ Customer (Root)       │    │
│  └─────────────────────────────────────────────────────┘    │
│  ┌─────────────────────────────────────────────────────┐    │
│  │               VALUE OBJECTS                         │    │
│  │  Money │ RentalPeriod │ VehicleSpec │ ContactInfo   │    │
│  └─────────────────────────────────────────────────────┘    │
│  ┌─────────────────────────────────────────────────────┐    │
│  │             DOMAIN SERVICES                         │    │
│  │  RentalPricingService                               │    │
│  └─────────────────────────────────────────────────────┘    │
│  ┌─────────────────────────────────────────────────────┐    │
│  │            DOMAIN EVENTS                            │    │
│  │  RentalCreated │ CarReturned                        │    │
│  └─────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────┘
```

## Aggregate Composition

### Rental Aggregate
```
┌─────────────────────────────────────┐
│        Rental (Aggregate Root)      │
│  - rentalId: String [ID]            │
│  - customerId: String [Ref]         │  ──► References Customer by ID
│  - carId: String [Ref]              │  ──► References Car by ID
│  ├─────────────────────────────────┤
│  │ RentalPeriod [Value Object]     │
│  │  - startDate: LocalDate         │
│  │  - endDate: LocalDate           │
│  ├─────────────────────────────────┤
│  │ Money [Value Object]            │
│  │  - amount: BigDecimal           │
│  │  - currency: String             │
│  ├─────────────────────────────────┤
│  - status: RentalStatus             │
│  - createdAt: LocalDateTime         │
│  + create()                         │
│  + startRental()                    │
│  + completeRental()                 │
│  + cancel()                         │
└─────────────────────────────────────┘
```

### Car Aggregate
```
┌─────────────────────────────────────┐
│          Car (Aggregate Root)       │
│  - carId: String [ID]               │
│  - vin: String                      │
│  - licensePlate: String             │
│  ├──────────────────────────────────┤
│  │ VehicleSpecification [VO]        │
│  │  - make: String                  │
│  │  - model: String                 │
│  │  - year: Integer                 │
│  │  - category: VehicleCategor y    │
│  ├──────────────────────────────────┤
│  │ Money [Value Object]             │
│  │  - dailyRate                     │
│  ├──────────────────────────────────┤
│  - availabilityStatus               │
│  - odometer: Integer                │
│  + create()                         │
│  + markAsRented()                   │
│  + markAsAvailable()                │
│  + sendToMaintenance()              │
└─────────────────────────────────────┘
```

### Customer Aggregate
```
┌─────────────────────────────────────┐
│       Customer (Aggregate Root)     │
│  - customerId: String [ID]          │
│  - firstName: String                │
│  - lastName: String                 │
│  - dateOfBirth: LocalDate           │
│  ├──────────────────────────────────┤
│  │ ContactInfo [Value Object]       │
│  │  - email: String                 │
│  │  - phone: String                 │
│  ├──────────────────────────────────┤
│  │ LicenseInfo [Value Object]       │
│  │  - licenseNumber: String         │
│  │  - issuingCountry: String        │
│  │  - expirationDate: LocalDate     │
│  ├──────────────────────────────────┤
│  - verified: boolean                │
│  + create()                         │
│  + verify()                         │
│  + isEligibleToRent()               │
└─────────────────────────────────────┘
```

## Domain Event Flow

```
1. User creates rental request
   │
   ▼
2. RentalService.createRental()
   │
   ├──► Validate Customer (eligibility check)
   ├──► Validate Car (availability check)
   ├──► Calculate Price (RentalPricingService)
   │
   ▼
3. Create Rental Aggregate
   │
   ▼
4. Update Car status to RENTED
   │
   ▼
5. Publish RentalCreated Event
   │
   ├──► Send confirmation email (future)
   ├──► Update analytics (future)
   └──► Notify billing system (future)
```

---

## CQRS Architecture Diagrams

### Complete CQRS Implementation Diagram

```
┌──────────────────────────────────────────────────────────────────────────────────┐
│                              CLIENT APPLICATIONS                                 │
│                         (Web, Mobile, External Systems)                          │
└────────────────┬────────────────────────────────────┬────────────────────────────┘
                 │                                    │
                 │ Commands (POST/PUT/DELETE)         │ Queries (GET)
                 │                                    │
                 ▼                                    ▼
┌────────────────────────────────────┐   ┌──────────────────────────────────────┐
│      WRITE SIDE (Commands)         │   │       READ SIDE (Queries)            │
│    ┌──────────────────────────┐    │   │   ┌──────────────────────────────┐   │
│    │ RentalCommandController  │    │   │   │  RentalQueryController       │   │
│    │  (REST API - Port 8080)  │    │   │   │   (REST API - Port 8080)     │   │
│    └───────────┬──────────────┘    │   │   └────────────┬─────────────────┘   │
│                │                   │   │                │                     │
│                ▼                   │   │                ▼                     │
│    ┌──────────────────────────┐    │   │   ┌──────────────────────────────┐   │
│    │   RentalCommandHandler   │    │   │   │    RentalQueryHandler        │   │
│    │  (Application Service)   │    │   │   │   (Application Service)      │   │
│    └───────────┬──────────────┘    │   │   └────────────┬─────────────────┘   │
│                │                   │   │                │                     │
│                ▼                   │   │                ▼                     │
│    ┌──────────────────────────┐    │   │   ┌──────────────────────────────┐   │
│    │    Rental Aggregate      │    │   │   │    RentalViewRepository      │   │
│    │   (Domain Model/Root)    │    │   │   │     (Query Repository)       │   │
│    │ ┌──────────────────────┐ │    │   │   └────────────┬─────────────────┘   │
│    │ │ Business Rules       │ │    │   │                │                     │
│    │ │ - Validation         │ │    │   │                ▼                     │
│    │ │ - State Transitions  │ │    │   │   ┌──────────────────────────────┐   │
│    │ │ - Domain Events      │ │    │   │   │  MongoDB (Read Database)     │   │
│    │ └──────────────────────┘ │    │   │   │  ┌────────────────────────┐  │   │
│    └───────────┬──────────────┘    │   │   │  │   Denormalized Views   │  │   │
│                │                   │   │   │  │  - RentalView          │  │   │
│                ▼                   │   │   │  │  - ActiveRentalsView   │  │   │
│    ┌──────────────────────────┐    │   │   │  │  - CustomerHistory     │  │   │
│    │   RentalRepository       │    │   │   │  └────────────────────────┘  │   │
│    │  (Write Repository)      │    │   │   └──────────────────────────────┘   │
│    └───────────┬──────────────┘    │   │                                      │
│                │                   │   │   Response: 200 OK with JSON         │
│                ▼                   │   │   (Denormalized, Query-Optimized)    │
│    ┌──────────────────────────┐    │   └──────────────────────────────────────┘
│    │  H2 Database (Write)     │    │                    ▲
│    │  ┌────────────────────┐  │    │                    │
│    │  │ Normalized Tables  │  │    │                    │
│    │  │ - rental           │  │    │                    │
│    │  │ - rental_period    │  │    │   ┌────────────────────────────────┐
│    │  └────────────────────┘  │    │   │   RentalProjectionUpdater      │
│    └──────────────────────────┘    │   │   (Event Consumer/Handler)     │
│                │                   │   │                                │
│                ▼                   │   │  @KafkaListener                │
│    ┌──────────────────────────┐    │   │  - Consumes Domain Events      │
│    │     EventPublisher       │    │   │  - Builds Denormalized Views   │
│    │   (Infrastructure)       │    │   │  - Updates Read Database       │
│    └───────────┬──────────────┘    │   └────────────┬───────────────────┘
│                │                   │                │
│                │ Publish Events    │                │ Consume Events
│                ▼                   │                │
└────────────────────────────────────┘                │
                 │                                    │
                 │                                    │
         ┌───────▼────────────────────────────────────▼─────────┐
         │           Apache Kafka (Message Broker)              │
         │  Topics:                                             │
         │  - rental-events                                     │
         │  - fleet-events                                      │
         │  - customer-events                                   │
         │                                                      │
         │  Events Published:                                   │
         │  ✓ RentalCreatedEvent                                │
         │  ✓ RentalStartedEvent                                │
         │  ✓ RentalCompletedEvent                              │
         │  ✓ RentalCancelledEvent                              │
         └──────────────────────────────────────────────────────┘

Response: 202 ACCEPTED (Command Acknowledged)
          Command processing happens asynchronously
```

### Event Flow Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         COMPLETE EVENT FLOW                                 │
└─────────────────────────────────────────────────────────────────────────────┘

  1. Command Execution                  2. Event Publishing
  ──────────────────                   ──────────────────

  POST /api/commands/rentals            Domain Event Created
         │                              ┌─────────────────────┐
         ▼                              │ RentalCreatedEvent  │
  CommandController                     │ - eventId           │
         │                              │ - rentalId          │
         ▼                              │ - customerId        │
  CommandHandler                        │ - carId             │
         │                              │ - timestamp         │
         ▼                              │ - metadata          │
  Execute Business Logic                └──────────┬──────────┘
  ┌────────────────────┐                          │
  │ 1. Validate        │                          ▼
  │ 2. Create Rental   │                  EventPublisher
  │ 3. Save to H2      │                          │
  │ 4. Create Event    │                          │
  │ 5. Publish Event   │                          ▼
  └────────────────────┘                  Kafka Producer
         │                              ┌─────────────────────┐
         ▼                              │ Topic: rental-events│
  Return 202 ACCEPTED                   │ Partition: 0        │
                                        │ Offset: 12345       │
                                        └──────────┬──────────┘
                                                   │
  ──────────────────────────────────────────────────────────────────────
                                                   │
  3. Event Consumption                  4. Projection Update
  ────────────────────                  ───────────────────

                                                   │
                                                   ▼
                                        Kafka Consumer
                                                   │
                                                   ▼
                                        @KafkaListener
                                        ProjectionUpdater
                                                   │
                                        ┌──────────▼──────────┐
                                        │ 1. Deserialize Event│
                                        │ 2. Fetch Related    │
                                        │    Data (if needed) │
                                        │ 3. Build View       │
                                        │ 4. Save to MongoDB  │
                                        └──────────┬──────────┘
                                                   │
                                                   ▼
                                        MongoDB Save
                                        ┌─────────────────────┐
                                        │ Collection:         │
                                        │   rentalView        │
                                        │                     │
                                        │ Document:           │
                                        │ {                   │
                                        │   rentalId: "...",  │
                                        │   customerId: "..." │
                                        │   customerName: "…" │
                                        │   carDetails: {...} │
                                        │   status: "ACTIVE"  │
                                        │   ...               │
                                        │ }                   │
                                        └─────────────────────┘

  5. Query Execution
  ──────────────────

  GET /api/queries/rentals/{id}
         │
         ▼
  QueryController
         │
         ▼
  QueryHandler
         │
         ▼
  Query MongoDB (Instant)
         │
         ▼
  Return 200 OK + Denormalized Data
```

### Write Side Flow (Commands)
```
┌──────────────────────────────────────────────────────────────┐
│                    WRITE SIDE DETAILED FLOW                  │
└──────────────────────────────────────────────────────────────┘

HTTP POST /api/commands/rentals
Content-Type: application/json
{
  "customerId": "cust-123",
  "carId": "car-456",
  "startDate": "2025-11-01",
  "endDate": "2025-11-05"
}
         │
         ▼
┌─────────────────────────────────────┐
│  RentalCommandController            │  @RestController
│  - Maps HTTP to Command Objects     │  @PostMapping
└──────────────┬──────────────────────┘
               │
               ▼ CreateRentalCommand
┌─────────────────────────────────────┐
│  RentalCommandHandler               │  @Service
│  - Orchestrates Use Case            │  @Transactional
│                                     │
│  Steps:                             │
│  1. Validate Command                │
│  2. Load Dependencies (if needed)   │
│  3. Create Domain Object            │
│  4. Execute Business Logic          │
│  5. Persist Changes                 │
│  6. Publish Domain Events           │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│  Rental (Aggregate Root)            │  Pure Domain Logic
│                                     │
│  Rental.create(                     │  Factory Method
│    customerId,                      │
│    carId,                           │
│    period,                          │
│    pricingService                   │  Domain Service
│  )                                  │
│                                     │
│  Business Rules Enforced:           │
│  ✓ Valid rental period              │
│  ✓ Non-null references              │
│  ✓ Calculate pricing                │
│  ✓ Set initial status (RESERVED)    │
│  ✓ Create domain event              │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│  RentalRepository.save(rental)      │  @Repository
│  - JPA Persistence                  │  Infrastructure
│  - H2 Database (Normalized)         │
│                                     │
│  Tables:                            │
│  - rental (id, customer_id, ...)    │
│  - rental_period (start, end)       │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│  EventPublisher.publish(event)      │  @Component
│  - Serialize Event to JSON          │  Infrastructure
│  - Send to Kafka Topic              │
│  - Topic: "rental-events"           │
└──────────────┬──────────────────────┘
               │
               ▼
Response: HTTP 202 ACCEPTED
{
  "rentalId": "rental-789",
  "message": "Rental creation initiated. Check query API for status."
}

⏱️  Total Time: ~50-100ms
    - Domain Logic: ~5ms
    - H2 Write: ~20ms
    - Kafka Publish: ~25ms
```

### Read Side Flow (Queries)
```
┌──────────────────────────────────────────────────────────────┐
│                    READ SIDE DETAILED FLOW                   │
└──────────────────────────────────────────────────────────────┘

Background Process (Event Consumption):
┌─────────────────────────────────────┐
│  @KafkaListener                     │  Async Consumer
│  topic = "rental-events"            │
│  groupId = "rental-projection"      │
└──────────────┬──────────────────────┘
               │
               ▼ RentalCreatedEvent
┌─────────────────────────────────────┐
│  RentalProjectionUpdater            │  @Component
│  - Event Handler                    │
│                                     │
│  Steps:                             │
│  1. Deserialize Event               │
│  2. Fetch Customer Data (if needed) │
│  3. Fetch Car Data (if needed)      │
│  4. Build Denormalized View         │
│  5. Save to MongoDB                 │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│  RentalView (Read Model)            │  @Document
│                                     │
│  Denormalized Document:             │
│  {                                  │
│    rentalId: "...",                 │
│    customerId: "...",               │
│    customerName: "John Doe",        │ ← Denormalized
│    customerEmail: "...",            │ ← from Customer
│    customerPhone: "...",            │ ← Context
│    carId: "...",                    │
│    carMake: "Toyota",               │ ← Denormalized
│    carModel: "Camry",               │ ← from Fleet
│    carYear: 2024,                   │ ← Context
│    startDate: "...",                │
│    totalCost: { amount: ..., },     │
│    status: "ACTIVE",                │
│    createdAt: "...",                │
│    lastUpdated: "..."               │
│  }                                  │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│  MongoDB Collection: rentalView     │
│  - No joins required                │
│  - Query-optimized                  │
│  - Indexed by rentalId, customerId  │
└─────────────────────────────────────┘

────────────────────────────────────────

Query Execution:

HTTP GET /api/queries/rentals/{rental-id}
         │
         ▼
┌─────────────────────────────────────┐
│  RentalQueryController              │  @RestController
│  - Maps HTTP to Query Objects       │  @GetMapping
└──────────────┬──────────────────────┘
               │
               ▼ GetRentalQuery
┌─────────────────────────────────────┐
│  RentalQueryHandler                 │  @Service
│  - Executes Query                   │
│  - No Business Logic                │
│  - Just Data Retrieval              │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│  RentalViewRepository               │  @Repository
│  .findByRentalId(rentalId)          │  MongoDB
│                                     │
│  Indexed Query:                     │
│  db.rentalView.findOne(             │
│    { rentalId: "rental-789" }       │
│  )                                  │
└──────────────┬──────────────────────┘
               │
               ▼
Response: HTTP 200 OK
{
  "rentalId": "rental-789",
  "customerId": "cust-123",
  "customerName": "John Doe",
  "customerEmail": "john@example.com",
  "customerPhone": "+1234567890",
  "carId": "car-456",
  "carMake": "Toyota",
  "carModel": "Camry",
  "carYear": 2024,
  "carCategory": "MIDSIZE",
  "startDate": "2025-11-01",
  "endDate": "2025-11-05",
  "totalCostAmount": 200.00,
  "status": "ACTIVE",
  ...
}

⏱️  Total Time: ~10-20ms
    - No joins
    - Single document lookup
    - All data pre-computed
```