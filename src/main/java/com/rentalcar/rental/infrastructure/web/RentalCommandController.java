package com.rentalcar.rental.infrastructure.web;

import com.rentalcar.rental.command.api.*;
import com.rentalcar.rental.command.handler.RentalCommandHandler;
import com.rentalcar.rental.infrastructure.web.dto.CompleteRentalRequest;
import com.rentalcar.rental.infrastructure.web.dto.CreateRentalRequest;
import com.rentalcar.rental.infrastructure.web.dto.CreateRentalResponse;
import com.rentalcar.rental.infrastructure.web.dto.StartRentalRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * COMMAND CONTROLLER: RentalCommandController
 *
 * Handles HTTP requests for WRITE operations (Commands).
 * This is part of the Command (Write) side of CQRS.
 *
 * Endpoints:
 * - POST /api/commands/rentals - Create rental
 * - POST /api/commands/rentals/{id}/start - Start rental
 * - POST /api/commands/rentals/{id}/complete - Complete rental
 * - DELETE /api/commands/rentals/{id} - Cancel rental
 */
@Slf4j
@RestController
@RequestMapping("/api/commands/rentals")
@RequiredArgsConstructor
public class RentalCommandController {

    private final RentalCommandHandler commandHandler;

    /**
     * Create a new rental
     */
    @PostMapping
    public ResponseEntity<CreateRentalResponse> createRental(
            @RequestBody CreateRentalRequest request) {

        log.info("Received create rental request: {}", request);

        try {
            CreateRentalCommand command = CreateRentalCommand.builder()
                    .rentalId(UUID.randomUUID().toString())
                    .customerId(request.customerId())
                    .carId(request.carId())
                    .startDate(request.startDate())
                    .endDate(request.endDate())
                    .build();

            String rentalId = commandHandler.handle(command);

            return ResponseEntity
                    .status(HttpStatus.ACCEPTED) // 202 - Command accepted for processing
                    .body(new CreateRentalResponse(rentalId,
                            "Rental creation initiated. Check query API for status."));

        } catch (IllegalArgumentException | IllegalStateException ex) {
            log.error("Error creating rental: {}", ex.getMessage());
            return ResponseEntity
                    .badRequest()
                    .build();
        }
    }

    /**
     * Start a rental (car pickup)
     */
    @PostMapping("/{rentalId}/start")
    public ResponseEntity<Void> startRental(
            @PathVariable String rentalId,
            @RequestBody StartRentalRequest request) {

        log.info("Received start rental request for: {}", rentalId);

        try {
            StartRentalCommand command = StartRentalCommand.builder()
                    .rentalId(rentalId)
                    .startOdometer(request.startOdometer())
                    .build();

            commandHandler.handle(command);

            return ResponseEntity.accepted().build();

        } catch (IllegalArgumentException | IllegalStateException ex) {
            log.error("Error starting rental: {}", ex.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Complete a rental (car return)
     */
    @PostMapping("/{rentalId}/complete")
    public ResponseEntity<Void> completeRental(
            @PathVariable String rentalId,
            @RequestBody CompleteRentalRequest request) {

        log.info("Received complete rental request for: {}", rentalId);

        try {
            CompleteRentalCommand command = CompleteRentalCommand.builder()
                    .rentalId(rentalId)
                    .endOdometer(request.endOdometer())
                    .build();

            commandHandler.handle(command);

            return ResponseEntity.accepted().build();

        } catch (IllegalArgumentException | IllegalStateException ex) {
            log.error("Error completing rental: {}", ex.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Cancel a rental
     */
    @DeleteMapping("/{rentalId}")
    public ResponseEntity<Void> cancelRental(
            @PathVariable String rentalId,
            @RequestParam(required = false, defaultValue = "Customer request") String reason) {

        log.info("Received cancel rental request for: {}", rentalId);

        try {
            CancelRentalCommand command = CancelRentalCommand.builder()
                    .rentalId(rentalId)
                    .reason(reason)
                    .build();

            commandHandler.handle(command);

            return ResponseEntity.accepted().build();

        } catch (IllegalArgumentException | IllegalStateException ex) {
            log.error("Error cancelling rental: {}", ex.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
