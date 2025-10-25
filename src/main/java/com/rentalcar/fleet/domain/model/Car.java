package com.rentalcar.fleet.domain.model;

import com.rentalcar.shared.domain.Money;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * ENTITY & AGGREGATE ROOT: Car
 * 
 * Represents a vehicle in the rental fleet.
 * Has unique identity (carId/VIN).
 * 
 * Aggregate boundary: Car is the root, contains VehicleSpecification (Value Object)
 * 
 * Business Rules:
 * - Car can only be rented if AVAILABLE
 * - Car maintains its availability status
 * - Car has a daily rental rate
 */
@Entity
@Table(name = "cars")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // For JPA
public class Car {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String carId;
    
    @Column(unique = true, nullable = false)
    private String vin; // Vehicle Identification Number
    
    @Column(nullable = false)
    private String licensePlate;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "year", column = @Column(name = "vehicle_year"))
    })
    private VehicleSpecification specification;
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "daily_rate_amount")),
        @AttributeOverride(name = "currency", column = @Column(name = "daily_rate_currency"))
    })
    private Money dailyRate;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AvailabilityStatus availabilityStatus;
    
    @Column(nullable = false)
    private Integer odometer;
    
    // Constructor for creating new car
    private Car(String vin, String licensePlate, VehicleSpecification specification, Money dailyRate) {
        this.vin = vin;
        this.licensePlate = licensePlate;
        this.specification = specification;
        this.dailyRate = dailyRate;
        this.availabilityStatus = AvailabilityStatus.AVAILABLE;
        this.odometer = 0;
    }
    
    /**
     * Factory method to create a new Car
     */
    public static Car create(String vin, String licensePlate, VehicleSpecification specification, BigDecimal dailyRateAmount, String currency) {
        validateVin(vin);
        validateLicensePlate(licensePlate);
        
        Money dailyRate = Money.of(dailyRateAmount, currency);
        return new Car(vin, licensePlate, specification, dailyRate);
    }
    
    /**
     * Domain behavior: Mark car as rented
     */
    public void markAsRented() {
        if (this.availabilityStatus != AvailabilityStatus.AVAILABLE) {
            throw new IllegalStateException("Car is not available for rental. Current status: " + this.availabilityStatus);
        }
        this.availabilityStatus = AvailabilityStatus.RENTED;
    }
    
    /**
     * Domain behavior: Mark car as available
     */
    public void markAsAvailable() {
        if (this.availabilityStatus == AvailabilityStatus.OUT_OF_SERVICE) {
            throw new IllegalStateException("Car is out of service and cannot be made available");
        }
        this.availabilityStatus = AvailabilityStatus.AVAILABLE;
    }
    
    /**
     * Domain behavior: Send car for maintenance
     */
    public void sendToMaintenance() {
        if (this.availabilityStatus == AvailabilityStatus.RENTED) {
            throw new IllegalStateException("Cannot send rented car to maintenance");
        }
        this.availabilityStatus = AvailabilityStatus.MAINTENANCE;
    }
    
    /**
     * Domain behavior: Update odometer reading
     */
    public void updateOdometer(Integer newReading) {
        if (newReading < this.odometer) {
            throw new IllegalArgumentException("New odometer reading cannot be less than current reading");
        }
        this.odometer = newReading;
    }
    
    /**
     * Query method: Check if car is available for rental
     */
    public boolean isAvailable() {
        return this.availabilityStatus == AvailabilityStatus.AVAILABLE;
    }
    
    private static void validateVin(String vin) {
        if (vin == null || vin.isBlank()) {
            throw new IllegalArgumentException("VIN cannot be null or empty");
        }
        if (vin.length() != 17) {
            throw new IllegalArgumentException("VIN must be 17 characters");
        }
    }
    
    private static void validateLicensePlate(String licensePlate) {
        if (licensePlate == null || licensePlate.isBlank()) {
            throw new IllegalArgumentException("License plate cannot be null or empty");
        }
    }
}
