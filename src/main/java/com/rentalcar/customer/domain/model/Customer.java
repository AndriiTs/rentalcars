package com.rentalcar.customer.domain.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * ENTITY & AGGREGATE ROOT: Customer
 * 
 * Represents a customer in the rental system.
 * Has unique identity (customerId).
 * 
 * Aggregate boundary: Customer is the root, contains:
 * - ContactInfo (Value Object)
 * - LicenseInfo (Value Object)
 * 
 * Business Rules:
 * - Customer must have valid license to rent
 * - Customer must be at least 21 years old
 */
@Entity
@Table(name = "customers")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // For JPA
public class Customer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String customerId;
    
    @Column(nullable = false)
    private String firstName;
    
    @Column(nullable = false)
    private String lastName;
    
    @Column(nullable = false)
    private LocalDate dateOfBirth;
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "email", column = @Column(name = "email", unique = true)),
        @AttributeOverride(name = "phone", column = @Column(name = "phone"))
    })
    private ContactInfo contactInfo;
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "licenseNumber", column = @Column(name = "license_number")),
        @AttributeOverride(name = "issuingCountry", column = @Column(name = "license_issuing_country")),
        @AttributeOverride(name = "expirationDate", column = @Column(name = "license_expiration"))
    })
    private LicenseInfo licenseInfo;
    
    @Column(nullable = false)
    private boolean verified;
    
    // Constructor for creating new customer
    private Customer(String firstName, String lastName, LocalDate dateOfBirth, 
                    ContactInfo contactInfo, LicenseInfo licenseInfo) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.contactInfo = contactInfo;
        this.licenseInfo = licenseInfo;
        this.verified = false;
    }
    
    /**
     * Factory method to create a new Customer
     */
    public static Customer create(String firstName, String lastName, LocalDate dateOfBirth,
                                 ContactInfo contactInfo, LicenseInfo licenseInfo) {
        validateName(firstName, "First name");
        validateName(lastName, "Last name");
        validateAge(dateOfBirth);
        
        return new Customer(firstName, lastName, dateOfBirth, contactInfo, licenseInfo);
    }
    
    /**
     * Domain behavior: Verify customer
     */
    public void verify() {
        if (!licenseInfo.isValid()) {
            throw new IllegalStateException("Cannot verify customer with invalid license");
        }
        this.verified = true;
    }
    
    /**
     * Domain behavior: Update contact information
     */
    public void updateContactInfo(ContactInfo newContactInfo) {
        this.contactInfo = newContactInfo;
    }
    
    /**
     * Domain behavior: Update license information
     */
    public void updateLicenseInfo(LicenseInfo newLicenseInfo) {
        this.licenseInfo = newLicenseInfo;
        // Revalidation might be needed after license update
        if (this.verified && !newLicenseInfo.isValid()) {
            this.verified = false;
        }
    }
    
    /**
     * Query method: Check if customer is eligible to rent
     */
    public boolean isEligibleToRent() {
        return verified && licenseInfo.isValid() && isAtLeast21YearsOld();
    }
    
    /**
     * Query method: Get customer's full name
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    /**
     * Query method: Get customer's age
     */
    public int getAge() {
        return LocalDate.now().getYear() - dateOfBirth.getYear();
    }
    
    private boolean isAtLeast21YearsOld() {
        return getAge() >= 21;
    }
    
    private static void validateName(String name, String fieldName) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be null or empty");
        }
        if (name.length() < 2) {
            throw new IllegalArgumentException(fieldName + " must be at least 2 characters");
        }
    }
    
    private static void validateAge(LocalDate dateOfBirth) {
        if (dateOfBirth == null) {
            throw new IllegalArgumentException("Date of birth cannot be null");
        }
        LocalDate minDate = LocalDate.now().minusYears(21);
        if (dateOfBirth.isAfter(minDate)) {
            throw new IllegalArgumentException("Customer must be at least 21 years old");
        }
        LocalDate maxDate = LocalDate.now().minusYears(100);
        if (dateOfBirth.isBefore(maxDate)) {
            throw new IllegalArgumentException("Invalid date of birth");
        }
    }
}
