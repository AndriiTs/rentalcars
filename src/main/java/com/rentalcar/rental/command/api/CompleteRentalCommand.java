package com.rentalcar.rental.command.api;

import lombok.Builder;
import lombok.Value;

/**
 * COMMAND: CompleteRentalCommand
 *
 * Represents the intention to complete a rental (car return).
 * Transitions rental from ACTIVE to COMPLETED status.
 */
@Value
@Builder
public class CompleteRentalCommand {
    String rentalId;
    Integer endOdometer;
}
