package com.rentalcar.rental.domain.service;

import com.rentalcar.rental.domain.model.RentalPeriod;
import com.rentalcar.shared.domain.Money;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * DOMAIN SERVICE: RentalPricingService
 * 
 * Domain service for calculating rental pricing.
 * This is a domain service because the pricing calculation logic doesn't
 * naturally belong to any single entity - it involves both Car's daily rate
 * and RentalPeriod duration.
 * 
 * Domain services contain domain logic that doesn't fit naturally into
 * entities or value objects.
 */
@Service
public class RentalPricingService {
    
    private static final BigDecimal WEEKLY_DISCOUNT = new BigDecimal("0.10"); // 10% discount
    private static final BigDecimal MONTHLY_DISCOUNT = new BigDecimal("0.20"); // 20% discount
    private static final int WEEKLY_THRESHOLD = 7;
    private static final int MONTHLY_THRESHOLD = 30;
    
    /**
     * Calculate total rental cost based on daily rate and rental period
     * Applies discounts for longer rentals
     * 
     * Business Rules:
     * - 10% discount for rentals >= 7 days
     * - 20% discount for rentals >= 30 days
     * 
     * @param dailyRate - Car's daily rental rate
     * @param rentalPeriod - Rental duration
     * @return Total rental cost
     */
    public Money calculateTotalCost(Money dailyRate, RentalPeriod rentalPeriod) {
        if (dailyRate == null) {
            throw new IllegalArgumentException("Daily rate cannot be null");
        }
        if (rentalPeriod == null) {
            throw new IllegalArgumentException("Rental period cannot be null");
        }
        
        long days = rentalPeriod.getDurationInDays();
        BigDecimal daysMultiplier = BigDecimal.valueOf(days);
        
        // Calculate base cost
        Money baseCost = dailyRate.multiply(daysMultiplier);
        
        // Apply discount based on duration
        BigDecimal discountRate = getDiscountRate(days);
        if (discountRate.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal discountAmount = baseCost.getAmount().multiply(discountRate);
            BigDecimal finalAmount = baseCost.getAmount().subtract(discountAmount);
            return Money.of(finalAmount, baseCost.getCurrency());
        }
        
        return baseCost;
    }
    
    /**
     * Calculate cost per day (useful for displaying to customers)
     */
    public Money calculateCostPerDay(Money totalCost, RentalPeriod rentalPeriod) {
        if (totalCost == null || rentalPeriod == null) {
            throw new IllegalArgumentException("Total cost and rental period cannot be null");
        }
        
        long days = rentalPeriod.getDurationInDays();
        BigDecimal costPerDay = totalCost.getAmount().divide(
            BigDecimal.valueOf(days), 
            2, 
            java.math.RoundingMode.HALF_UP
        );
        
        return Money.of(costPerDay, totalCost.getCurrency());
    }
    
    /**
     * Determine discount rate based on rental duration
     */
    private BigDecimal getDiscountRate(long days) {
        if (days >= MONTHLY_THRESHOLD) {
            return MONTHLY_DISCOUNT;
        } else if (days >= WEEKLY_THRESHOLD) {
            return WEEKLY_DISCOUNT;
        }
        return BigDecimal.ZERO;
    }
    
    /**
     * Get discount percentage as a readable value (for display)
     */
    public int getDiscountPercentage(long days) {
        BigDecimal rate = getDiscountRate(days);
        return rate.multiply(BigDecimal.valueOf(100)).intValue();
    }
}
