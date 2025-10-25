package com.rentalcar.rental.command.api;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

/**
 * COMMAND: CreateRentalCommand
 *
 * Represents the intention to create a new rental.
 * Commands are imperatives - they express what should be done.
 *
 * This command will:
 * 1. Validate customer and car availability
 * 2. Calculate rental pricing
 * 3. Create rental aggregate
 * 4. Publish RentalCreatedEvent
 */
@Value
@Builder
public class CreateRentalCommand {
    String rentalId;
    String customerId;
    String carId;
    LocalDate startDate;
    LocalDate endDate;
}
