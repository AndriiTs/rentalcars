package com.rentalcar.rental.domain.event;

import com.rentalcar.shared.event.DomainEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DOMAIN EVENT: RentalStartedEvent
 *
 * Published when customer picks up the car and rental becomes active.
 * This event triggers:
 * - Read model updates (status change to ACTIVE)
 * - Odometer reading capture
 * - Insurance activation
 */
@Getter
@NoArgsConstructor
public class RentalStartedEvent extends DomainEvent {

    private Integer startOdometer;
    private LocalDateTime pickedUpAt;

    public RentalStartedEvent(String rentalId, Integer startOdometer, LocalDateTime pickedUpAt) {
        super(rentalId, "Rental");
        this.startOdometer = startOdometer;
        this.pickedUpAt = pickedUpAt;
    }

    @Override
    public String getTopicName() {
        return "rental-events";
    }
}
