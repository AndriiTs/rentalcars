# Car Rental Service - Project Summary

## File Structure

```
rental-car-ddd/
├── README.md                          # Main documentation
├── CONCEPTS.md                        # DDD concepts explained
├── ARCHITECTURE.md                    # Architecture diagrams
├── PROJECT_SUMMARY.md                 # This file
├── pom.xml                            # Maven configuration
└── src/main/
    ├── java/com/rentalcar/
    │   ├── RentalCarApplication.java  # Main application (1 file)
    │   │
    │   ├── rental/                    # RENTAL CONTEXT (8 files)
    │   │   ├── domain/
    │   │   │   ├── model/
    │   │   │   │   ├── Rental.java              # Aggregate Root
    │   │   │   │   ├── RentalPeriod.java        # Value Object
    │   │   │   │   └── RentalStatus.java        # Enum
    │   │   │   ├── repository/
    │   │   │   │   └── RentalRepository.java    # Interface
    │   │   │   ├── service/
    │   │   │   │   └── RentalPricingService.java # Domain Service
    │   │   │   └── event/
    │   │   │       └── RentalCreated.java       # Domain Event
    │   │   ├── application/
    │   │   │   └── RentalService.java           # Application Service
    │   │   └── infrastructure/
    │   │       ├── persistence/
    │   │       │   └── JpaRentalRepository.java # JPA impl
    │   │       └── web/
    │   │           └── RentalController.java    # REST API
    │   │
    │   ├── fleet/                     # FLEET CONTEXT (7 files)
    │   │   ├── domain/
    │   │   │   ├── model/
    │   │   │   │   ├── Car.java                 # Aggregate Root
    │   │   │   │   ├── VehicleSpecification.java # Value Object
    │   │   │   │   ├── VehicleCategory.java     # Enum
    │   │   │   │   └── AvailabilityStatus.java  # Enum
    │   │   │   └── repository/
    │   │   │       └── CarRepository.java       # Interface
    │   │   ├── application/
    │   │   │   └── FleetService.java            # Application Service
    │   │   └── infrastructure/
    │   │       ├── persistence/
    │   │       │   └── JpaCarRepository.java    # JPA impl
    │   │       └── web/
    │   │           └── FleetController.java     # REST API
    │   │
    │   ├── customer/                  # CUSTOMER CONTEXT (7 files)
    │   │   ├── domain/
    │   │   │   ├── model/
    │   │   │   │   ├── Customer.java            # Aggregate Root
    │   │   │   │   ├── ContactInfo.java         # Value Object
    │   │   │   │   └── LicenseInfo.java         # Value Object
    │   │   │   └── repository/
    │   │   │       └── CustomerRepository.java  # Interface
    │   │   ├── application/
    │   │   │   └── CustomerService.java         # Application Service
    │   │   └── infrastructure/
    │   │       ├── persistence/
    │   │       │   └── JpaCustomerRepository.java # JPA impl
    │   │       └── web/
    │   │           └── CustomerController.java  # REST API
    │   │
    │   └── shared/                    # SHARED KERNEL (1 file)
    │       └── domain/
    │           └── Money.java                   # Shared Value Object
    │
    └── resources/
        └── application.properties               # Spring config
```

## Bounded Contexts in Detail

### 1. Rental Management Context (Core Domain) 🎯

**Location**: `com.rentalcar.rental`

**Purpose**: Core business logic for rental transactions - this is where the business makes money

**Components**:
- **Domain Layer**:
  - `Rental` (Aggregate Root) - Manages rental lifecycle
  - `RentalPeriod` (Value Object) - Encapsulates date range
  - `RentalStatus` (Enum) - RESERVED, ACTIVE, COMPLETED, CANCELLED
  - `RentalPricingService` (Domain Service) - Complex pricing logic with discounts
  - `RentalCreated` (Domain Event) - Published when rental created
  - `RentalRepository` (Interface) - Persistence abstraction

- **Application Layer**:
  - `RentalService` - Orchestrates use cases (create, start, complete, cancel)

- **Infrastructure Layer**:
  - `JpaRentalRepository` - Spring Data JPA implementation
  - `RentalController` - REST API endpoints

### 2. Fleet Management Context (Supporting) 🚗

**Location**: `com.rentalcar.fleet`

**Purpose**: Manage vehicle inventory and availability

**Components**:
- **Domain Layer**:
  - `Car` (Aggregate Root) - Vehicle with identity and state
  - `VehicleSpecification` (Value Object) - Make, model, year, category
  - `VehicleCategory` (Enum) - ECONOMY, COMPACT, MIDSIZE, FULLSIZE, SUV, LUXURY, VAN
  - `AvailabilityStatus` (Enum) - AVAILABLE, RENTED, MAINTENANCE, OUT_OF_SERVICE
  - `CarRepository` (Interface) - Persistence abstraction

- **Application Layer**:
  - `FleetService` - Manages fleet operations

- **Infrastructure Layer**:
  - `JpaCarRepository` - Spring Data JPA implementation
  - `FleetController` - REST API endpoints

### 3. Customer Management Context (Supporting) 👤

**Location**: `com.rentalcar.customer`

**Purpose**: Manage customer information and eligibility

**Components**:
- **Domain Layer**:
  - `Customer` (Aggregate Root) - Customer with identity and verification
  - `ContactInfo` (Value Object) - Email and phone
  - `LicenseInfo` (Value Object) - License details and expiration
  - `CustomerRepository` (Interface) - Persistence abstraction

- **Application Layer**:
  - `CustomerService` - Manages customer operations

- **Infrastructure Layer**:
  - `JpaCustomerRepository` - Spring Data JPA implementation
  - `CustomerController` - REST API endpoints

## 🔑 Key DDD Patterns Demonstrated

### ✅ Entities (With Identity)
```java
@Entity
public class Rental {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String rentalId;  // ← Unique identifier

    private String customerId; // ← Reference by ID only
    private String carId;      // ← Reference by ID only
}
```

### Value Objects (Immutable, No Identity)
```java
@Embeddable
public class RentalPeriod {
    private LocalDate startDate;
    private LocalDate endDate;
    // No setters - immutable
    // Equality based on values
}
```

### Aggregates (Consistency Boundaries)
```
Rental Aggregate (Root: Rental)
 ├── rentalId (Identity)
 ├── customerId (Reference by ID)
 ├── carId (Reference by ID)
 ├── RentalPeriod (Value Object)
 ├── Money (Value Object)
 └── RentalStatus (Enum)
```

### Domain Services (Stateless Business Logic)
```java
@Service
public class RentalPricingService {
    public Money calculateTotalCost(Money dailyRate, RentalPeriod period) {
        long days = period.getDurationInDays();
        BigDecimal discount = calculateDiscount(days);
        // Complex pricing rules
    }
}
```

### Domain Events (Things That Happened)
```java
public record RentalCreated(
    String rentalId,
    String customerId,
    String carId,
    LocalDateTime occurredOn
) {}
```

### Repositories (Persistence Abstraction)
```java
// Domain interface - what we need
public interface RentalRepository {
    Rental save(Rental rental);
    Optional<Rental> findById(String id);
}

// Infrastructure implementation - how it works
@Repository
public interface JpaRentalRepository
    extends JpaRepository<Rental, String>, RentalRepository {}
```

## Architecture Layers

```
┌─────────────────────────────────────┐
│   INFRASTRUCTURE LAYER              │
│   • REST Controllers                │
│   • JPA Repositories                │
│   • Event Publishers                │
│   • Configuration                   │
└──────────────┬──────────────────────┘
               │ depends on
┌──────────────▼──────────────────────┐
│   APPLICATION LAYER                 │
│   • RentalService                   │
│   • FleetService                    │
│   • CustomerService                 │
│   (Use case orchestration)          │
└──────────────┬──────────────────────┘
               │ depends on
┌──────────────▼──────────────────────┐
│   DOMAIN LAYER                      │
│   • Entities (Rental, Car, Customer)│
│   • Value Objects (Money, Period...)│
│   • Domain Services (Pricing)       │
│   • Repository Interfaces           │
│   • Domain Events                   │
│   (Pure business logic)             │
└─────────────────────────────────────┘
```

## How to Run

### Prerequisites
- Java 17 or higher
- Maven 3.6+

### Steps
```bash
# Clone/navigate to project directory
cd rental-car-ddd

# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

**Application URLs**:
- API: `http://localhost:8080`
- H2 Console: `http://localhost:8080/h2-console`
  - JDBC URL: `jdbc:h2:mem:rentaldb`
  - Username: `sa`
  - Password: (empty)

## 📝 Complete API Reference
For complete API documentation with request/response examples, see **[API_EXAMPLES.md](API_EXAMPLES.md)**

## 📚 Key Takeaways

1. **Bounded Contexts** - Separate models for different business areas
2. **Aggregates** - Define transactional consistency boundaries
3. **Reference by ID** - Loose coupling between aggregates
4. **Value Objects** - Encapsulate concepts without identity
5. **Domain Services** - Stateless operations on multiple aggregates
6. **Repositories** - Persistence abstraction at aggregate level
7. **Domain Events** - Communicate between contexts
8. **Ubiquitous Language** - Business terms in code
9. **Layered Architecture** - Separation of concerns
10. **Rich Domain Model** - Business logic in domain, not services

## 📖 Next Steps


1. **Extend the System**:
   - Add payment processing
   - Implement insurance options
   - Add reservation system
   - Create reporting features

2. **Write Tests**:
   - Unit tests for domain logic
   - Integration tests for repositories
   - API tests for controllers

