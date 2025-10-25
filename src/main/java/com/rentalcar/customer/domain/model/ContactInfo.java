package com.rentalcar.customer.domain.model;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * VALUE OBJECT: ContactInfo
 * 
 * Immutable value object representing contact information.
 * No identity - equality based on attributes.
 */
@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED) // For JPA
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ContactInfo {
    
    private String email;
    private String phone;
    
    public static ContactInfo of(String email, String phone) {
        validateEmail(email);
        validatePhone(phone);
        return new ContactInfo(email, phone);
    }
    
    private static void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Invalid email format");
        }
    }
    
    private static void validatePhone(String phone) {
        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("Phone cannot be null or empty");
        }
        // Simple validation - can be enhanced
        if (!phone.matches("^[+]?[0-9]{10,15}$")) {
            throw new IllegalArgumentException("Invalid phone format");
        }
    }
    
    @Override
    public String toString() {
        return "Email: " + email + ", Phone: " + phone;
    }
}
