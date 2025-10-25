package com.rentalcar.rental.domain.model;

import com.rentalcar.shared.domain.Money;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ENTITY & AGGREGATE ROOT: Rental
 * 
 * Represents a car rental transaction - the core domain entity.
 * Has unique identity (rentalId).
 * 
 * AGGREGATE BOUNDARY: Rental is the root, contains:
 * - RentalPeriod (Value Object)
 * - Money (Value Object) - total cost
 * - References to Car (by ID, NOT direct reference)
 * - References to Customer (by ID, NOT direct reference)
 * 
 * IMPORTANT DDD RULE: Aggregates reference each other by ID only!
 * This maintains loose coupling and transactional boundaries.
 * 
 * Business Rules:
 * - Rental can only be created for available cars
 * - Customer must be eligible to rent
 * - Total cost is calculated based on rental period and car's daily rate
 * - Rental can be cancelled only if status is RESERVED
 * - Rental can be completed only if status is ACTIVE
 */
@Entity
@Table(name = "rentals")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // For JPA
public class Rental {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String rentalId;
    
    /**
     * Reference to Customer aggregate by ID (NOT a direct object reference)
     * This maintains aggregate boundaries and transactional consistency
     */
    @Column(nullable = false)
    private String customerId;
    
    /**
     * Reference to Car aggregate by ID (NOT a direct object reference)
     * This maintains aggregate boundaries and transactional consistency
     */
    @Column(nullable = false)
    private String carId;
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "startDate", column = @Column(name = "start_date")),
        @AttributeOverride(name = "endDate", column = @Column(name = "end_date"))
    })
    private RentalPeriod rentalPeriod;
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "total_cost_amount")),
        @AttributeOverride(name = "currency", column = @Column(name = "total_cost_currency"))
    })
    private Money totalCost;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RentalStatus status;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime pickedUpAt;
    
    private LocalDateTime returnedAt;
    
    private Integer startOdometer;
    
    private Integer endOdometer;
    
    // Constructor for creating new rental
    private Rental(String customerId, String carId, RentalPeriod rentalPeriod, Money totalCost) {
        this.customerId = customerId;
        this.carId = carId;
        this.rentalPeriod = rentalPeriod;
        this.totalCost = totalCost;
        this.status = RentalStatus.RESERVED;
        this.createdAt = LocalDateTime.now();
    }
    
    /**
     * Factory method to create a new Rental
     * 
     * @param customerId - Reference to Customer aggregate
     * @param carId - Reference to Car aggregate
     * @param rentalPeriod - Value object defining rental duration
     * @param totalCost - Value object defining total rental cost
     */
    public static Rental create(String customerId, String carId, RentalPeriod rentalPeriod, Money totalCost) {
        validateCustomerId(customerId);
        validateCarId(carId);
        
        return new Rental(customerId, carId, rentalPeriod, totalCost);
    }
    
    /**
     * Domain behavior: Start the rental (car pickup)
     */
    public void startRental(Integer startOdometer) {
        if (this.status != RentalStatus.RESERVED) {
            throw new IllegalStateException("Can only start a reserved rental. Current status: " + this.status);
        }
        if (startOdometer == null || startOdometer < 0) {
            throw new IllegalArgumentException("Invalid start odometer reading");
        }
        
        this.status = RentalStatus.ACTIVE;
        this.pickedUpAt = LocalDateTime.now();
        this.startOdometer = startOdometer;
    }
    
    /**
     * Domain behavior: Complete the rental (car return)
     */
    public void completeRental(Integer endOdometer) {
        if (this.status != RentalStatus.ACTIVE) {
            throw new IllegalStateException("Can only complete an active rental. Current status: " + this.status);
        }
        if (endOdometer == null || endOdometer < this.startOdometer) {
            throw new IllegalArgumentException("Invalid end odometer reading");
        }
        
        this.status = RentalStatus.COMPLETED;
        this.returnedAt = LocalDateTime.now();
        this.endOdometer = endOdometer;
    }
    
    /**
     * Domain behavior: Cancel the rental
     */
    public void cancel() {
        if (this.status != RentalStatus.RESERVED) {
            throw new IllegalStateException("Can only cancel a reserved rental. Current status: " + this.status);
        }
        
        this.status = RentalStatus.CANCELLED;
    }
    
    /**
     * Query method: Calculate distance driven
     */
    public Integer getDistanceDriven() {
        if (endOdometer == null || startOdometer == null) {
            return null;
        }
        return endOdometer - startOdometer;
    }
    
    /**
     * Query method: Check if rental is active
     */
    public boolean isActive() {
        return this.status == RentalStatus.ACTIVE;
    }
    
    /**
     * Query method: Check if rental can be modified
     */
    public boolean canBeModified() {
        return this.status == RentalStatus.RESERVED;
    }
    
    private static void validateCustomerId(String customerId) {
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("Customer ID cannot be null or empty");
        }
    }
    
    private static void validateCarId(String carId) {
        if (carId == null || carId.isBlank()) {
            throw new IllegalArgumentException("Car ID cannot be null or empty");
        }
    }
}
