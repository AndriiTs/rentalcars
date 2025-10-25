package com.rentalcar.rental.query.api;

import lombok.Value;

/**
 * QUERY: GetCustomerRentalsQuery
 *
 * Retrieves all rentals for a specific customer.
 */
@Value
public class GetCustomerRentalsQuery {
    String customerId;
}
