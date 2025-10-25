package com.rentalcar.rental.query.repository;

import com.rentalcar.rental.query.model.RentalView;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for RentalView (Read Model)
 *
 * Uses MongoDB for fast, flexible queries on denormalized data.
 * This is part of the Query (Read) side of CQRS.
 */
@Repository
public interface RentalViewRepository extends MongoRepository<RentalView, String> {

    /**
     * Find rentals by customer ID
     */
    List<RentalView> findByCustomerId(String customerId);

    /**
     * Find rentals by status
     */
    List<RentalView> findByStatus(String status);

    /**
     * Find active rentals
     */
    default List<RentalView> findActiveRentals() {
        return findByStatus("ACTIVE");
    }

    /**
     * Find rentals by car ID
     */
    List<RentalView> findByCarId(String carId);

    /**
     * Find rentals by date range
     */
    @Query("{ 'startDate': { $gte: ?0 }, 'endDate': { $lte: ?1 } }")
    List<RentalView> findByDateRange(LocalDate from, LocalDate to);

    /**
     * Find rentals by customer name (case-insensitive)
     */
    List<RentalView> findByCustomerNameContainingIgnoreCase(String customerName);
}
