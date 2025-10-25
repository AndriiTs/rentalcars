package com.rentalcar.rental.query.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * READ MODEL: RentalView
 *
 * Denormalized view of rental data optimized for queries.
 * This is the READ SIDE of CQRS.
 *
 * Stored in MongoDB for:
 * - Fast queries
 * - Flexible schema
 * - Easy denormalization
 *
 * Updated by Kafka consumers when domain events are published.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "rental_views")
public class RentalView {

    @Id
    private String rentalId;

    // Denormalized customer data
    private String customerId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;

    // Denormalized car data
    private String carId;
    private String carMake;
    private String carModel;
    private Integer carYear;
    private String carCategory;
    private String carLicensePlate;

    // Rental details
    private LocalDate startDate;
    private LocalDate endDate;
    private Long durationDays;

    // Pricing
    private BigDecimal totalCostAmount;
    private String totalCostCurrency;
    private String formattedTotalCost;

    // Status
    private String status; // RESERVED, ACTIVE, COMPLETED, CANCELLED

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime pickedUpAt;
    private LocalDateTime returnedAt;
    private LocalDateTime lastUpdated;

    // Odometer readings
    private Integer startOdometer;
    private Integer endOdometer;
    private Integer totalKilometers;

    // Additional info
    private String cancellationReason;
}
