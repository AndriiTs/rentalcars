package com.rentalcar.rental.domain.event;

import com.rentalcar.shared.event.DomainEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * DOMAIN EVENT: RentalCancelledEvent
 *
 * Published when a rental reservation is cancelled.
 * This event triggers:
 * - Read model updates (status change to CANCELLED)
 * - Car status update (mark as AVAILABLE)
 * - Refund processing
 * - Cancellation notification
 */
@Getter
@NoArgsConstructor
public class RentalCancelledEvent extends DomainEvent {

    private String reason;
    private String carId;

    public RentalCancelledEvent(String rentalId, String carId, String reason) {
        super(rentalId, "Rental");
        this.carId = carId;
        this.reason = reason;
    }

    @Override
    public String getTopicName() {
        return "rental-events";
    }
}
