package com.rentalcar.rental.infrastructure.web.dto;

import java.time.LocalDate;

public record CreateRentalRequest(
        String customerId,
        String carId,
        LocalDate startDate,
        LocalDate endDate
) {}
