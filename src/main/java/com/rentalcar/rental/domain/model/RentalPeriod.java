package com.rentalcar.rental.domain.model;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * VALUE OBJECT: RentalPeriod
 * 
 * Immutable value object representing the time period of a rental.
 * Encapsulates business logic related to rental duration.
 */
@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED) // For JPA
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RentalPeriod {
    
    private LocalDate startDate;
    private LocalDate endDate;
    
    public static RentalPeriod of(LocalDate startDate, LocalDate endDate) {
        validateDates(startDate, endDate);
        return new RentalPeriod(startDate, endDate);
    }
    
    /**
     * Calculate number of days in the rental period
     */
    public long getDurationInDays() {
        return ChronoUnit.DAYS.between(startDate, endDate) + 1; // Include both start and end date
    }
    
    /**
     * Check if rental period overlaps with another period
     */
    public boolean overlapsWith(RentalPeriod other) {
        return !this.endDate.isBefore(other.startDate) && !this.startDate.isAfter(other.endDate);
    }
    
    /**
     * Check if rental period is in the past
     */
    public boolean isPast() {
        return endDate.isBefore(LocalDate.now());
    }
    
    /**
     * Check if rental period is current (ongoing)
     */
    public boolean isCurrent() {
        LocalDate today = LocalDate.now();
        return !startDate.isAfter(today) && !endDate.isBefore(today);
    }
    
    /**
     * Check if rental period is in the future
     */
    public boolean isFuture() {
        return startDate.isAfter(LocalDate.now());
    }
    
    private static void validateDates(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and end date cannot be null");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
        if (startDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Start date cannot be in the past");
        }
        long days = ChronoUnit.DAYS.between(startDate, endDate);
        if (days > 365) {
            throw new IllegalArgumentException("Rental period cannot exceed 365 days");
        }
    }
    
    @Override
    public String toString() {
        return startDate + " to " + endDate + " (" + getDurationInDays() + " days)";
    }
}
