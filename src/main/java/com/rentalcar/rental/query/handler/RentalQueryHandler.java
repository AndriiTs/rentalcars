package com.rentalcar.rental.query.handler;

import com.rentalcar.rental.query.api.*;
import com.rentalcar.rental.query.model.RentalView;
import com.rentalcar.rental.query.repository.RentalViewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * QUERY HANDLER: RentalQueryHandler
 *
 * Handles queries for rental data.
 * This is the READ SIDE of CQRS.
 *
 * Responsibilities:
 * 1. Receive query
 * 2. Fetch data from read model (MongoDB)
 * 3. Return denormalized view
 *
 * No business logic - just data retrieval!
 *
 * Pattern: Query Handler in CQRS
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RentalQueryHandler {

    private final RentalViewRepository rentalViewRepository;

    /**
     * Handle GetRentalQuery
     */
    public RentalView handle(GetRentalQuery query) {
        log.info("Handling GetRentalQuery: {}", query.getRentalId());

        return rentalViewRepository.findById(query.getRentalId())
                .orElseThrow(() -> new IllegalArgumentException("Rental not found: " + query.getRentalId()));
    }

    /**
     * Handle GetActiveRentalsQuery
     */
    public List<RentalView> handle(GetActiveRentalsQuery query) {
        log.info("Handling GetActiveRentalsQuery");

        return rentalViewRepository.findActiveRentals();
    }

    /**
     * Handle GetCustomerRentalsQuery
     */
    public List<RentalView> handle(GetCustomerRentalsQuery query) {
        log.info("Handling GetCustomerRentalsQuery: {}", query.getCustomerId());

        return rentalViewRepository.findByCustomerId(query.getCustomerId());
    }
}
