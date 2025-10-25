package com.rentalcar.rental.infrastructure.web.dto;

import com.rentalcar.rental.domain.model.Rental;

import java.time.LocalDate;

public record RentalResponse(
        String rentalId,
        String customerId,
        String carId,
        LocalDate startDate,
        LocalDate endDate,
        String totalCost,
        String status,
        Long durationDays
) {
    public static RentalResponse fromDomain(Rental rental) {
        return new RentalResponse(
                rental.getRentalId(),
                rental.getCustomerId(),
                rental.getCarId(),
                rental.getRentalPeriod().getStartDate(),
                rental.getRentalPeriod().getEndDate(),
                rental.getTotalCost().toString(),
                rental.getStatus().name(),
                rental.getRentalPeriod().getDurationInDays()
        );
    }
}
