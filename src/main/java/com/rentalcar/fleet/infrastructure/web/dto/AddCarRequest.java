package com.rentalcar.fleet.infrastructure.web.dto;

import com.rentalcar.fleet.domain.model.VehicleCategory;

import java.math.BigDecimal;

public record AddCarRequest(
        String vin,
        String licensePlate,
        String make,
        String model,
        Integer year,
        VehicleCategory category,
        BigDecimal dailyRate,
        String currency
) {}
