package com.rentalcar.customer.infrastructure.web.dto;

import com.rentalcar.customer.domain.model.Customer;

import java.time.LocalDate;

public record CustomerResponse(
        String customerId,
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        String email,
        String phone,
        String licenseNumber,
        String issuingCountry,
        LocalDate licenseExpiration,
        boolean verified,
        Integer age
) {
    public static CustomerResponse fromDomain(Customer customer) {
        return new CustomerResponse(
                customer.getCustomerId(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getDateOfBirth(),
                customer.getContactInfo().getEmail(),
                customer.getContactInfo().getPhone(),
                customer.getLicenseInfo().getLicenseNumber(),
                customer.getLicenseInfo().getIssuingCountry(),
                customer.getLicenseInfo().getExpirationDate(),
                customer.isVerified(),
                customer.getAge()
        );
    }
}
