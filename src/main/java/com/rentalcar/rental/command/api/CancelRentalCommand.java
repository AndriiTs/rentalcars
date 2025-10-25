package com.rentalcar.rental.command.api;

import lombok.Builder;
import lombok.Value;

/**
 * COMMAND: CancelRentalCommand
 *
 * Represents the intention to cancel a rental reservation.
 * Only RESERVED rentals can be cancelled.
 */
@Value
@Builder
public class CancelRentalCommand {
    String rentalId;
    String reason;
}
