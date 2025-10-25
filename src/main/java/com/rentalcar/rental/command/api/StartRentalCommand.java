package com.rentalcar.rental.command.api;

import lombok.Builder;
import lombok.Value;

/**
 * COMMAND: StartRentalCommand
 *
 * Represents the intention to start a rental (car pickup).
 * Transitions rental from RESERVED to ACTIVE status.
 */
@Value
@Builder
public class StartRentalCommand {
    String rentalId;
    Integer startOdometer;
}
