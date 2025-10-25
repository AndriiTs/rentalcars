package com.rentalcar.rental.infrastructure.web;

import com.rentalcar.rental.application.RentalService;
import com.rentalcar.rental.domain.model.Rental;
import com.rentalcar.rental.infrastructure.web.dto.CompleteRentalRequest;
import com.rentalcar.rental.infrastructure.web.dto.CreateRentalRequest;
import com.rentalcar.rental.infrastructure.web.dto.RentalResponse;
import com.rentalcar.rental.infrastructure.web.dto.StartRentalRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * INFRASTRUCTURE: REST Controller for Rental operations
 * 
 * This is infrastructure/interface layer - not domain layer.
 * Controllers translate HTTP requests to domain operations.
 */
@Slf4j
@RestController
@RequestMapping("/api/rentals")
@RequiredArgsConstructor
public class RentalController {
    
    private final RentalService rentalService;
    
    /**
     * Create a new rental
     * POST /api/rentals
     */
    @PostMapping
    public ResponseEntity<RentalResponse> createRental(@RequestBody CreateRentalRequest request) {
        log.info("Received create rental request: {}", request);

        try {
            Rental rental = rentalService.createRental(
                    request.customerId(),
                    request.carId(),
                    request.startDate(),
                    request.endDate()
            );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(RentalResponse.fromDomain(rental));

        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("Error creating rental: {}", e.getMessage());
            return ResponseEntity
                    .badRequest()
                    .build();
        }
    }
    
    /**
     * Start rental (car pickup)
     * POST /api/rentals/{rentalId}/start
     */
    @PostMapping("/{rentalId}/start")
    public ResponseEntity<Void> startRental(
            @PathVariable String rentalId,
            @RequestBody StartRentalRequest request) {

        log.info("Starting rental: {}", rentalId);

        try {
            rentalService.startRental(rentalId, request.startOdometer());
            return ResponseEntity.ok().build();

        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("Error starting rental: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Complete rental (car return)
     * POST /api/rentals/{rentalId}/complete
     */
    @PostMapping("/{rentalId}/complete")
    public ResponseEntity<Void> completeRental(
            @PathVariable String rentalId,
            @RequestBody CompleteRentalRequest request) {

        log.info("Completing rental: {}", rentalId);

        try {
            rentalService.completeRental(rentalId, request.endOdometer());
            return ResponseEntity.ok().build();

        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("Error completing rental: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Cancel rental
     * DELETE /api/rentals/{rentalId}
     */
    @DeleteMapping("/{rentalId}")
    public ResponseEntity<Void> cancelRental(@PathVariable String rentalId) {
        log.info("Cancelling rental: {}", rentalId);
        
        try {
            rentalService.cancelRental(rentalId);
            return ResponseEntity.noContent().build();
            
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("Error cancelling rental: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get customer's rentals
     * GET /api/rentals/customer/{customerId}
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<RentalResponse>> getCustomerRentals(@PathVariable String customerId) {
        List<Rental> rentals = rentalService.getCustomerRentals(customerId);
        List<RentalResponse> responses = rentals.stream()
                .map(RentalResponse::fromDomain)
                .toList();
        
        return ResponseEntity.ok(responses);
    }
    
    /**
     * Get all active rentals
     * GET /api/rentals/active
     */
    @GetMapping("/active")
    public ResponseEntity<List<RentalResponse>> getActiveRentals() {
        List<Rental> rentals = rentalService.getActiveRentals();
        List<RentalResponse> responses = rentals.stream()
                .map(RentalResponse::fromDomain)
                .toList();
        
        return ResponseEntity.ok(responses);
    }
}
