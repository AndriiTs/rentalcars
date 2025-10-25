package com.rentalcar.rental.domain.event;

import com.rentalcar.shared.event.DomainEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DOMAIN EVENT: RentalCompletedEvent
 *
 * Published when customer returns the car and rental is completed.
 * This event triggers:
 * - Read model updates (status change to COMPLETED)
 * - Car status update (mark as AVAILABLE)
 * - Final payment processing
 * - Receipt generation
 */
@Getter
@NoArgsConstructor
public class RentalCompletedEvent extends DomainEvent {

    private Integer endOdometer;
    private LocalDateTime returnedAt;
    private String carId;

    public RentalCompletedEvent(String rentalId, String carId, Integer endOdometer, LocalDateTime returnedAt) {
        super(rentalId, "Rental");
        this.carId = carId;
        this.endOdometer = endOdometer;
        this.returnedAt = returnedAt;
    }

    @Override
    public String getTopicName() {
        return "rental-events";
    }
}
