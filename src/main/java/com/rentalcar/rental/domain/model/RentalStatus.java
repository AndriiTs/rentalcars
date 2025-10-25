package com.rentalcar.rental.domain.model;

/**
 * Represents the status of a rental throughout its lifecycle
 */
public enum RentalStatus {
    /**
     * Rental has been created but not yet started
     */
    RESERVED,
    
    /**
     * Rental is currently active (car picked up)
     */
    ACTIVE,
    
    /**
     * Rental has been completed and car returned
     */
    COMPLETED,
    
    /**
     * Rental was cancelled before starting
     */
    CANCELLED
}
