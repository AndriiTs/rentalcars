package com.rentalcar.customer.domain.model;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * VALUE OBJECT: LicenseInfo
 * 
 * Immutable value object representing driver's license information.
 */
@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED) // For JPA
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LicenseInfo {
    
    private String licenseNumber;
    private String issuingCountry;
    private LocalDate expirationDate;
    
    public static LicenseInfo of(String licenseNumber, String issuingCountry, LocalDate expirationDate) {
        validateLicenseNumber(licenseNumber);
        validateIssuingCountry(issuingCountry);
        validateExpirationDate(expirationDate);
        return new LicenseInfo(licenseNumber, issuingCountry, expirationDate);
    }
    
    /**
     * Check if license is currently valid
     */
    public boolean isValid() {
        return LocalDate.now().isBefore(expirationDate);
    }
    
    private static void validateLicenseNumber(String licenseNumber) {
        if (licenseNumber == null || licenseNumber.isBlank()) {
            throw new IllegalArgumentException("License number cannot be null or empty");
        }
    }
    
    private static void validateIssuingCountry(String issuingCountry) {
        if (issuingCountry == null || issuingCountry.isBlank()) {
            throw new IllegalArgumentException("Issuing country cannot be null or empty");
        }
    }
    
    private static void validateExpirationDate(LocalDate expirationDate) {
        if (expirationDate == null) {
            throw new IllegalArgumentException("Expiration date cannot be null");
        }
        if (expirationDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("License is expired");
        }
    }
    
    @Override
    public String toString() {
        return licenseNumber + " (" + issuingCountry + "), expires: " + expirationDate;
    }
}
