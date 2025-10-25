package com.rentalcar.rental.infrastructure.persistence;

import com.rentalcar.rental.domain.model.Rental;
import com.rentalcar.rental.domain.model.RentalStatus;
import com.rentalcar.rental.domain.repository.RentalRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * INFRASTRUCTURE: JpaRentalRepository
 * 
 * JPA implementation of RentalRepository domain interface.
 */
@Repository
public interface JpaRentalRepository extends JpaRepository<Rental, String>, RentalRepository {
    
    @Override
    List<Rental> findByCustomerId(String customerId);

    @Override
    List<Rental> findByCarId(String carId);

    @Override
    List<Rental> findByStatus(RentalStatus status);
}
