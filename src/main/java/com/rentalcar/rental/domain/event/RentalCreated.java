package com.rentalcar.rental.domain.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * DOMAIN EVENT: RentalCreated
 * 
 * Domain event representing that a new rental has been created.
 * 
 * Domain events represent something significant that happened in the domain.
 * They can be used to:
 * - Notify other bounded contexts
 * - Trigger side effects (e.g., send confirmation email)
 * - Maintain audit logs
 * - Achieve eventual consistency between aggregates
 * 
 * Domain events are immutable and represent facts that have occurred.
 */
@Getter
@AllArgsConstructor
public class RentalCreated {
    
    private final String rentalId;
    private final String customerId;
    private final String carId;
    private final LocalDateTime occurredAt;
    
    public static RentalCreated of(String rentalId, String customerId, String carId) {
        return new RentalCreated(rentalId, customerId, carId, LocalDateTime.now());
    }
    
    @Override
    public String toString() {
        return String.format("RentalCreated[rentalId=%s, customerId=%s, carId=%s, occurredAt=%s]",
                rentalId, customerId, carId, occurredAt);
    }
}
