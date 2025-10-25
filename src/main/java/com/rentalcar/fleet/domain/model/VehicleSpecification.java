package com.rentalcar.fleet.domain.model;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * VALUE OBJECT: VehicleSpecification
 * 
 * Immutable value object describing vehicle characteristics.
 * No identity - equality based on attributes.
 */
@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED) // For JPA
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class VehicleSpecification {
    
    private String make;
    private String model;
    private Integer year;
    private VehicleCategory category;
    
    public static VehicleSpecification of(String make, String model, Integer year, VehicleCategory category) {
        validateMake(make);
        validateModel(model);
        validateYear(year);
        validateCategory(category);
        return new VehicleSpecification(make, model, year, category);
    }
    
    private static void validateMake(String make) {
        if (make == null || make.isBlank()) {
            throw new IllegalArgumentException("Make cannot be null or empty");
        }
    }
    
    private static void validateModel(String model) {
        if (model == null || model.isBlank()) {
            throw new IllegalArgumentException("Model cannot be null or empty");
        }
    }
    
    private static void validateYear(Integer year) {
        if (year == null) {
            throw new IllegalArgumentException("Year cannot be null");
        }
        int currentYear = java.time.Year.now().getValue();
        if (year < 1900 || year > currentYear + 1) {
            throw new IllegalArgumentException("Invalid year: " + year);
        }
    }
    
    private static void validateCategory(VehicleCategory category) {
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }
    }
    
    @Override
    public String toString() {
        return year + " " + make + " " + model + " (" + category + ")";
    }
}
