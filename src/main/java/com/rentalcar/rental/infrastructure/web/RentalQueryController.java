package com.rentalcar.rental.infrastructure.web;

import com.rentalcar.rental.query.api.*;
import com.rentalcar.rental.query.handler.RentalQueryHandler;
import com.rentalcar.rental.query.model.RentalView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * QUERY CONTROLLER: RentalQueryController
 *
 * Handles HTTP requests for READ operations (Queries).
 * This is part of the Query (Read) side of CQRS.
 *
 * Endpoints:
 * - GET /api/queries/rentals/{id} - Get rental by ID
 * - GET /api/queries/rentals/active - Get all active rentals
 * - GET /api/queries/rentals/customer/{customerId} - Get customer's rentals
 *
 * Key Differences from Command Controller:
 * - Returns 200 OK (not 202 ACCEPTED)
 * - Reads from MongoDB (denormalized views)
 * - No business logic - just data retrieval
 * - Fast, optimized queries
 */
@Slf4j
@RestController
@RequestMapping("/api/queries/rentals")
@RequiredArgsConstructor
public class RentalQueryController {

    private final RentalQueryHandler queryHandler;

    /**
     * Get rental by ID
     * Returns denormalized view with all customer and car details
     */
    @GetMapping("/{rentalId}")
    public ResponseEntity<RentalView> getRental(@PathVariable String rentalId) {
        log.info("Received query for rental: {}", rentalId);

        try {
            GetRentalQuery query = new GetRentalQuery(rentalId);
            RentalView view = queryHandler.handle(query);

            return ResponseEntity.ok(view);

        } catch (IllegalArgumentException ex) {
            log.error("Rental not found: {}", rentalId);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get all active rentals
     * Returns list of rentals with status = ACTIVE
     */
    @GetMapping("/active")
    public ResponseEntity<List<RentalView>> getActiveRentals() {
        log.info("Received query for active rentals");

        GetActiveRentalsQuery query = new GetActiveRentalsQuery();
        List<RentalView> views = queryHandler.handle(query);

        return ResponseEntity.ok(views);
    }

    /**
     * Get all rentals for a customer
     * Returns complete rental history for the customer
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<RentalView>> getCustomerRentals(
            @PathVariable String customerId) {

        log.info("Received query for customer rentals: {}", customerId);

        GetCustomerRentalsQuery query = new GetCustomerRentalsQuery(customerId);
        List<RentalView> views = queryHandler.handle(query);

        return ResponseEntity.ok(views);
    }
}
