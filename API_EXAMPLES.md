# API Examples - Car Rental Service

This document provides complete API examples for the Car Rental Service. The API follows the **CQRS pattern** with separate endpoints for Commands (write operations) and Queries (read operations).

## Base URL

```
http://localhost:8080
```

## Table of Contents

- [Commands (Write Side)](#commands-write-side)
  - [Rental Management](#rental-management)
  - [Fleet Management](#fleet-management)
  - [Customer Management](#customer-management)
- [Queries (Read Side)](#queries-read-side)
  - [Rental Queries](#rental-queries)
  - [Fleet Queries](#fleet-queries)

---

## Commands (Write Side)

Commands modify state and return `202 ACCEPTED` to indicate the request is being processed asynchronously.

### Rental Management

#### Create a Rental

Reserve a car for a customer for a specific period.

```bash
curl -X POST http://localhost:8080/api/commands/rentals \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "{customer-id}",
    "carId": "{car-id}",
    "startDate": "2025-11-01",
    "endDate": "2025-11-05"
  }'
```

**Response:** `202 ACCEPTED`
```json
{
  "rentalId": "abc-123-def-456",
  "message": "Rental creation initiated. Check query API for status."
}
```

**Business Rules:**
- Customer must be verified and eligible (21+ years old)
- Car must be available
- Rental period must be valid (start date before end date)
- Pricing includes discounts: 10% for 7+ days, 20% for 30+ days

---

#### Start Rental (Car Pickup)

Mark a rental as started when the customer picks up the car.

```bash
curl -X POST http://localhost:8080/api/commands/rentals/{rental-id}/start \
  -H "Content-Type: application/json" \
  -d '{
    "startOdometer": 15000
  }'
```

**Response:** `202 ACCEPTED`
```json
{
  "message": "Rental started successfully."
}
```

**Business Rules:**
- Rental must be in RESERVED status
- Odometer reading is required

---

#### Complete Rental (Car Return)

Mark a rental as completed when the customer returns the car.

```bash
curl -X POST http://localhost:8080/api/commands/rentals/{rental-id}/complete \
  -H "Content-Type: application/json" \
  -d '{
    "endOdometer": 15250
  }'
```

**Response:** `202 ACCEPTED`
```json
{
  "message": "Rental completed successfully."
}
```

**Business Rules:**
- Rental must be in ACTIVE status
- End odometer must be greater than start odometer

---

#### Cancel Rental

Cancel a reserved rental.

```bash
curl -X DELETE http://localhost:8080/api/commands/rentals/{rental-id}?reason=Customer%20request
```

**Response:** `202 ACCEPTED`
```json
{
  "message": "Rental cancellation initiated."
}
```

**Business Rules:**
- Can only cancel RESERVED rentals
- Cannot cancel ACTIVE or COMPLETED rentals

---

### Fleet Management

#### Add a Car to Fleet

Register a new car in the fleet.

```bash
curl -X POST http://localhost:8080/api/fleet/cars \
  -H "Content-Type: application/json" \
  -d '{
    "vin": "1HGBH41JXMN109186",
    "licensePlate": "ABC123",
    "make": "Toyota",
    "model": "Camry",
    "year": 2024,
    "category": "MIDSIZE",
    "dailyRate": 50.00,
    "currency": "USD"
  }'
```

**Response:** `201 CREATED`
```json
{
  "carId": "car-456",
  "message": "Car added to fleet successfully."
}
```

**Business Rules:**
- VIN must be exactly 17 characters
- VIN must be unique
- License plate must be unique
- Daily rate must be positive

---

#### Send Car to Maintenance

Mark a car as unavailable for maintenance.

```bash
curl -X POST http://localhost:8080/api/fleet/cars/{car-id}/maintenance
```

**Response:** `202 ACCEPTED`
```json
{
  "message": "Car sent to maintenance."
}
```

**Business Rules:**
- Car must be AVAILABLE (cannot send RENTED cars to maintenance)

---

#### Return Car from Maintenance

Mark a car as available after maintenance.

```bash
curl -X POST http://localhost:8080/api/fleet/cars/{car-id}/available
```

**Response:** `202 ACCEPTED`
```json
{
  "message": "Car marked as available."
}
```

---

### Customer Management

#### Register a Customer

Register a new customer in the system.

```bash
curl -X POST http://localhost:8080/api/customers \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "dateOfBirth": "1990-01-15",
    "email": "john.doe@example.com",
    "phone": "+11234567890",
    "licenseNumber": "D1234567",
    "issuingCountry": "USA",
    "licenseExpiration": "2026-12-31"
  }'
```

**Response:** `201 CREATED`
```json
{
  "customerId": "cust-789",
  "message": "Customer registered successfully."
}
```

**Business Rules:**
- Must be at least 21 years old
- Email must be unique
- Driver's license must not be expired

---

#### Verify a Customer

Verify a customer's identity and license.

```bash
curl -X POST http://localhost:8080/api/customers/{customer-id}/verify
```

**Response:** `202 ACCEPTED`
```json
{
  "message": "Customer verification initiated."
}
```

**Business Rules:**
- Customer must have valid license
- Must be at least 21 years old

---

## Queries (Read Side)

Queries retrieve data and return `200 OK` with denormalized data from MongoDB for fast retrieval.

### Rental Queries

#### Get Rental Details

Retrieve complete rental information including denormalized customer and car data.

```bash
curl http://localhost:8080/api/queries/rentals/{rental-id}
```

**Response:** `200 OK`
```json
{
  "rentalId": "abc-123-def-456",
  "customerId": "cust-789",
  "customerName": "John Doe",
  "customerEmail": "john.doe@example.com",
  "customerPhone": "+11234567890",
  "carId": "car-456",
  "carMake": "Toyota",
  "carModel": "Camry",
  "carYear": 2024,
  "carCategory": "MIDSIZE",
  "carLicensePlate": "ABC123",
  "startDate": "2025-11-01",
  "endDate": "2025-11-05",
  "durationDays": 4,
  "totalCostAmount": 200.00,
  "totalCostCurrency": "USD",
  "formattedTotalCost": "USD 200.00",
  "status": "ACTIVE",
  "startOdometer": 15000,
  "pickedUpAt": "2025-11-01T10:30:00",
  "createdAt": "2025-10-25T14:20:00",
  "lastUpdated": "2025-11-01T10:30:15"
}
```

**Note:** All data is denormalized in a single document - no joins required!

---

#### Get All Active Rentals

Retrieve all currently active rentals.

```bash
curl http://localhost:8080/api/queries/rentals/active
```

**Response:** `200 OK`
```json
[
  {
    "rentalId": "abc-123-def-456",
    "customerName": "John Doe",
    "carMake": "Toyota",
    "carModel": "Camry",
    "startDate": "2025-11-01",
    "endDate": "2025-11-05",
    "status": "ACTIVE"
  }
]
```

---

#### Get Customer's Rental History

Retrieve all rentals for a specific customer.

```bash
curl http://localhost:8080/api/queries/rentals/customer/{customer-id}
```

**Response:** `200 OK`
```json
[
  {
    "rentalId": "abc-123-def-456",
    "carMake": "Toyota",
    "carModel": "Camry",
    "startDate": "2025-11-01",
    "endDate": "2025-11-05",
    "totalCost": "USD 200.00",
    "status": "COMPLETED"
  },
  {
    "rentalId": "xyz-789-uvw-012",
    "carMake": "Honda",
    "carModel": "Accord",
    "startDate": "2025-10-15",
    "endDate": "2025-10-20",
    "totalCost": "USD 250.00",
    "status": "COMPLETED"
  }
]
```

---

### Fleet Queries

#### Get Available Cars

Retrieve all cars available for rental.

```bash
curl http://localhost:8080/api/fleet/cars/available
```

**Response:** `200 OK`
```json
[
  {
    "carId": "car-456",
    "make": "Toyota",
    "model": "Camry",
    "year": 2024,
    "category": "MIDSIZE",
    "licensePlate": "ABC123",
    "dailyRate": {
      "amount": 50.00,
      "currency": "USD"
    },
    "availabilityStatus": "AVAILABLE"
  },
  {
    "carId": "car-789",
    "make": "Honda",
    "model": "Accord",
    "year": 2024,
    "category": "MIDSIZE",
    "licensePlate": "XYZ789",
    "dailyRate": {
      "amount": 55.00,
      "currency": "USD"
    },
    "availabilityStatus": "AVAILABLE"
  }
]
```

---

#### Get Car Details

Retrieve detailed information about a specific car.

```bash
curl http://localhost:8080/api/fleet/cars/{car-id}
```

**Response:** `200 OK`
```json
{
  "carId": "car-456",
  "vin": "1HGBH41JXMN109186",
  "licensePlate": "ABC123",
  "make": "Toyota",
  "model": "Camry",
  "year": 2024,
  "category": "MIDSIZE",
  "dailyRate": {
    "amount": 50.00,
    "currency": "USD"
  },
  "availabilityStatus": "AVAILABLE",
  "odometer": 15000,
  "createdAt": "2025-01-01T10:00:00"
}
```

---

## Response Times

**CQRS Performance Benefits:**

- **Commands (Write):** ~50-100ms
  - Domain Logic: ~5ms
  - H2 Write: ~20ms
  - Kafka Publish: ~25ms
  - Returns `202 ACCEPTED` immediately

- **Queries (Read):** ~10-20ms
  - No joins required
  - Single document lookup from MongoDB
  - All data pre-computed and denormalized
  - Returns `200 OK` with complete data

---

## Error Responses

All endpoints may return error responses:

**400 Bad Request** - Invalid input data
```json
{
  "error": "Bad Request",
  "message": "Customer must be at least 21 years old",
  "timestamp": "2025-10-27T10:30:00"
}
```

**404 Not Found** - Resource not found
```json
{
  "error": "Not Found",
  "message": "Rental with ID 'abc-123' not found",
  "timestamp": "2025-10-27T10:30:00"
}
```

**409 Conflict** - Business rule violation
```json
{
  "error": "Conflict",
  "message": "Car is not available for rental",
  "timestamp": "2025-10-27T10:30:00"
}
```

**500 Internal Server Error** - Unexpected server error
```json
{
  "error": "Internal Server Error",
  "message": "An unexpected error occurred",
  "timestamp": "2025-10-27T10:30:00"
}
```

---

## Testing the Complete Flow

Here's a complete example workflow:

```bash
# 1. Register a customer
curl -X POST http://localhost:8080/api/customers \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "dateOfBirth": "1990-01-15",
    "email": "john.doe@example.com",
    "phone": "+11234567890",
    "licenseNumber": "D1234567",
    "issuingCountry": "USA",
    "licenseExpiration": "2026-12-31"
  }'
# Returns: customerId

# 2. Verify the customer
curl -X POST http://localhost:8080/api/customers/{customer-id}/verify

# 3. Add a car to fleet
curl -X POST http://localhost:8080/api/fleet/cars \
  -H "Content-Type: application/json" \
  -d '{
    "vin": "1HGBH41JXMN109186",
    "licensePlate": "ABC123",
    "make": "Toyota",
    "model": "Camry",
    "year": 2024,
    "category": "MIDSIZE",
    "dailyRate": 50.00,
    "currency": "USD"
  }'
# Returns: carId

# 4. Create a rental
curl -X POST http://localhost:8080/api/commands/rentals \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "{customer-id}",
    "carId": "{car-id}",
    "startDate": "2025-11-01",
    "endDate": "2025-11-05"
  }'
# Returns: rentalId

# 5. Query the rental (wait ~100ms for eventual consistency)
curl http://localhost:8080/api/queries/rentals/{rental-id}

# 6. Start the rental (car pickup)
curl -X POST http://localhost:8080/api/commands/rentals/{rental-id}/start \
  -H "Content-Type: application/json" \
  -d '{"startOdometer": 15000}'

# 7. Complete the rental (car return)
curl -X POST http://localhost:8080/api/commands/rentals/{rental-id}/complete \
  -H "Content-Type: application/json" \
  -d '{"endOdometer": 15250}'

# 8. View customer's rental history
curl http://localhost:8080/api/queries/rentals/customer/{customer-id}
```

---

## Additional Resources

- **[README.md](README.md)** - Project overview and getting started
- **[ARCHITECTURE.md](ARCHITECTURE.md)** - Detailed architecture and CQRS diagrams
- **[CONCEPTS.md](CONCEPTS.md)** - DDD concepts and patterns
- **[PROJECT_SUMMARY.md](PROJECT_SUMMARY.md)** - Complete project summary
