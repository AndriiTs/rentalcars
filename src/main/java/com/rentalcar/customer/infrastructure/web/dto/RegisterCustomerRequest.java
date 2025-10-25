package com.rentalcar.customer.infrastructure.web.dto;

import java.time.LocalDate;

public record RegisterCustomerRequest(
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        String email,
        String phone,
        String licenseNumber,
        String issuingCountry,
        LocalDate licenseExpiration
) {}
