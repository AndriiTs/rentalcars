package com.rentalcar.rental.domain.event;

import com.rentalcar.shared.event.DomainEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DOMAIN EVENT: RentalCreatedEvent
 *
 * Published when a new rental reservation is created.
 * This event triggers:
 * - Read model updates (rental view projections)
 * - Car status update (mark as RENTED)
 * - Notification to customer (confirmation email)
 * - Analytics tracking
 */
@Getter
@NoArgsConstructor
public class RentalCreatedEvent extends DomainEvent {

    private String customerId;
    private String carId;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalCostAmount;
    private String totalCostCurrency;
    private String status; // RESERVED

    public RentalCreatedEvent(String rentalId, String customerId, String carId,
                              LocalDate startDate, LocalDate endDate,
                              BigDecimal totalCostAmount, String totalCostCurrency) {
        super(rentalId, "Rental");
        this.customerId = customerId;
        this.carId = carId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalCostAmount = totalCostAmount;
        this.totalCostCurrency = totalCostCurrency;
        this.status = "RESERVED";
    }

    @Override
    public String getTopicName() {
        return "rental-events";
    }
}
