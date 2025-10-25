# Domain-Driven Design Concepts — Car Rental Service

This document explains the DDD concepts demonstrated in this project with concrete examples from the codebase.

## Domain

The **Car Rental Business** - everything related to renting vehicles to customers, managing a fleet of cars, and handling customer relationships.

## 🏗️ Bounded Contexts

The system is divided into three distinct bounded contexts, each with its own domain model:

### 1. Rental Management Context (Core Domain)

**Location**: `com.rentalcar.rental`

**Purpose**: Core business logic for managing rental transactions

**Domain Model**:
- **Aggregate Root**: `Rental`
- **Value Objects**: `RentalPeriod`, `Money` (shared), `RentalStatus` (enum)
- **Domain Service**: `RentalPricingService` - complex pricing with discounts
- **Domain Event**: `RentalCreated`
- **Repository**: `RentalRepository`

**Key Operations**:
- Create rental reservation
- Start rental (car pickup)
- Complete rental (car return)
- Cancel rental
- Calculate pricing with discounts (7+ days: 10%, 30+ days: 20%)

### 2. Fleet Management Context (Supporting) 🚗

**Location**: `com.rentalcar.fleet`

**Purpose**: Manage vehicle inventory and availability

**Domain Model**:
- **Aggregate Root**: `Car`
- **Value Objects**: `VehicleSpecification`, `AvailabilityStatus` (enum), `VehicleCategory` (enum), `Money` (shared)
- **Repository**: `CarRepository`

**Key Operations**:
- Add car to fleet
- Mark car as rented/available
- Send car to maintenance
- Track odometer readings

### 3. Customer Management Context (Supporting) 👤

**Location**: `com.rentalcar.customer`

**Purpose**: Manage customer information and verification

**Domain Model**:
- **Aggregate Root**: `Customer`
- **Value Objects**: `ContactInfo`, `LicenseInfo`
- **Repository**: `CustomerRepository`

**Key Operations**:
- Register new customer
- Verify customer (KYC)
- Validate age (21+)
- Validate license expiration

## 📚 DDD Building Blocks

### Entities

**Objects with unique identity that persists over time**

#### Example: `Rental` Entity
```java
@Entity
public class Rental {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String rentalId;  // Unique identity

    private String customerId; // Reference by ID
    private String carId;      // Reference by ID
    // ... more fields
}
```

**Key Characteristics**:
- Has unique ID (rentalId, carId, customerId)
- Mutable state can change over time
- Two entities with same data but different IDs are NOT equal
- Identity matters more than attributes

**Examples in this project**:
- `Rental` - identified by `rentalId`
- `Car` - identified by `carId` (UUID)
- `Customer` - identified by `customerId`

### Value Objects

**Immutable objects without identity, defined purely by their attributes**

#### Example: `Money` Value Object
```java
@Embeddable
public class Money {
    private BigDecimal amount;
    private String currency;

    // Immutable - no setters!
    // Equality based on attributes, not identity
}
```

#### Example: `RentalPeriod` Value Object
```java
@Embeddable
public class RentalPeriod {
    private LocalDate startDate;
    private LocalDate endDate;

    public long getDurationInDays() {
        return ChronoUnit.DAYS.between(startDate, endDate);
    }
}
```

**Key Characteristics**:
- No unique identifier
- Immutable (no setters)
- Equality based on ALL attributes
- Can contain business logic
- Replaceable (not modifiable)

**Examples in this project**:
- `Money` - amount + currency (shared kernel)
- `RentalPeriod` - startDate + endDate
- `VehicleSpecification` - make, model, year, category
- `ContactInfo` - email, phone
- `LicenseInfo` - licenseNumber, country, expiration

### Aggregates & Aggregate Roots

**A cluster of entities and value objects with a consistency boundary**

#### Rental Aggregate
```
Rental (Aggregate Root)
 ├── rentalId (Identity)
 ├── customerId (Reference by ID)
 ├── carId (Reference by ID)
 ├── RentalPeriod (Value Object)
 ├── Money (Value Object)
 └── RentalStatus (Enum)
```

**Key Rules**:
- External objects reference the aggregate by ID only
- All changes go through the root
- Root enforces invariants
- One aggregate per transaction

#### Car Aggregate
```
Car (Aggregate Root)
 ├── carId (Identity)
 ├── vin (Natural key)
 ├── licensePlate
 ├── VehicleSpecification (Value Object)
 ├── AvailabilityStatus (Enum)
 ├── Money dailyRate (Value Object)
 └── odometer (int)
```

#### Customer Aggregate
```
Customer (Aggregate Root)
 ├── customerId (Identity)
 ├── firstName, lastName
 ├── dateOfBirth
 ├── ContactInfo (Value Object)
 ├── LicenseInfo (Value Object)
 └── verified (boolean)
```

### Domain Services

**Operations that don't naturally belong to any entity**

#### Example: `RentalPricingService`
```java
@Service
public class RentalPricingService {
    public Money calculateTotalCost(Money dailyRate, RentalPeriod period) {
        long days = period.getDurationInDays();
        BigDecimal discount = calculateDiscount(days);
        // Complex pricing logic
    }
}
```

**When to use**:
- Operation involves multiple aggregates
- Doesn't naturally fit in one entity
- Stateless operations
- Complex calculations

**Example in this project**:
- `RentalPricingService` - calculates costs with business rules (discounts for longer rentals)

### Repositories

**Abstraction for persisting and retrieving aggregates**

#### Domain Layer (Interface)
```java
public interface RentalRepository {
    Rental save(Rental rental);
    Optional<Rental> findById(String rentalId);
    List<Rental> findByCustomerId(String customerId);
    List<Rental> findActiveRentals();
}
```

#### Infrastructure Layer (Implementation)
```java
@Repository
public interface JpaRentalRepository
    extends RentalRepository, JpaRepository<Rental, String> {
    // Spring Data JPA implementation
}
```

**Key Principles**:
- Interface defined in domain layer
- Implementation in infrastructure layer
- Works with aggregate roots only
- Hides persistence details

**Examples in this project**:
- `RentalRepository` / `JpaRentalRepository`
- `CarRepository` / `JpaCarRepository`
- `CustomerRepository` / `JpaCustomerRepository`

### Domain Events

**Something that happened in the domain that domain experts care about**

#### Example: `RentalCreated` Event
```java
public record RentalCreated(
    String rentalId,
    String customerId,
    String carId,
    LocalDateTime occurredOn
) {
    // Published when rental is created
    // Could trigger: notifications, analytics, etc.
}
```

**Key Characteristics**:
- Past tense naming (CarRented, not RentCar)
- Immutable
- Contains event data
- Can trigger side effects in other contexts

**Example in this project**:
- `RentalCreated` - published when a new rental is created

## 🔗 Aggregate References

### ❌ Wrong Way - Direct Object References
```java
public class Rental {
    private Customer customer;  // BAD - tight coupling
    private Car car;            // BAD - crosses aggregate boundary
}
```

**Problems**:
- Tight coupling between aggregates
- Hard to maintain consistency
- Can't update aggregates independently
- Transaction boundaries unclear

###  Right Way - Reference by ID
```java
public class Rental {
    private String customerId;  // GOOD - loose coupling
    private String carId;       // GOOD - clear boundary
}
```

**Benefits**:
- Clear aggregate boundaries
- Independent evolution
- Easier to scale
- Clear transaction boundaries

## 🏛️ Layered Architecture

### Domain Layer
**Pure business logic, framework-independent**
- Entities, Value Objects, Aggregates
- Domain Services
- Repository Interfaces
- Domain Events

**Location**: `*.domain.*`

### Application Layer
**Use case orchestration**
- Application Services
- Coordinate domain objects
- Manage transactions
- Publish events

**Location**: `*.application.*`

### Infrastructure Layer
**Technical concerns**
- Repository Implementations (JPA)
- REST Controllers
- Configuration
- External integrations

**Location**: `*.infrastructure.*`

## 🗣️ Ubiquitous Language

**Use business terminology consistently across code and conversations**

| Business Term | Code Implementation |
|---------------|---------------------|
| Rental Reservation | `Rental` entity with `RESERVED` status |
| Car Pickup | `rental.startRental()` |
| Car Return | `rental.completeRental()` |
| Fleet | `fleet` bounded context |
| Daily Rate | `Money dailyRate` |
| Customer Verification | `customer.verify()` |
| Send to Maintenance | `car.sendToMaintenance()` |

## 📊 Summary

**3 Bounded Contexts** - Rental (core), Fleet, Customer (supporting)

**3 Aggregate Roots** - Rental, Car, Customer

**6 Value Objects** - Money, RentalPeriod, VehicleSpecification, ContactInfo, LicenseInfo, + enums

**1 Domain Service** - RentalPricingService

**3 Repositories** - One per aggregate

**1 Domain Event** - RentalCreated

**Clean Architecture** - Domain → Application → Infrastructure

**Reference by ID** - No direct aggregate references

**Ubiquitous Language** - Business terms in code
