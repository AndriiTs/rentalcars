package com.rentalcar.rental.query.api;

import lombok.Value;

/**
 * QUERY: GetRentalQuery
 *
 * Queries are questions we ask about the current state.
 * They don't change anything - they just retrieve data.
 */
@Value
public class GetRentalQuery {
    String rentalId;
}
