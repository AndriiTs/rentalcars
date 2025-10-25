package com.rentalcar.fleet.infrastructure.web.dto;

import com.rentalcar.fleet.domain.model.Car;

public record CarResponse(
        String carId,
        String vin,
        String licensePlate,
        String make,
        String model,
        Integer year,
        String category,
        String availabilityStatus,
        String dailyRate,
        Integer odometer
) {
    public static CarResponse fromDomain(Car car) {
        return new CarResponse(
                car.getCarId(),
                car.getVin(),
                car.getLicensePlate(),
                car.getSpecification().getMake(),
                car.getSpecification().getModel(),
                car.getSpecification().getYear(),
                car.getSpecification().getCategory().name(),
                car.getAvailabilityStatus().name(),
                car.getDailyRate().toString(),
                car.getOdometer()
        );
    }
}
