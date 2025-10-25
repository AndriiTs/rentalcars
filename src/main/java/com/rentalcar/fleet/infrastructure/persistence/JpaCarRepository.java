package com.rentalcar.fleet.infrastructure.persistence;

import com.rentalcar.fleet.domain.model.AvailabilityStatus;
import com.rentalcar.fleet.domain.model.Car;
import com.rentalcar.fleet.domain.repository.CarRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * INFRASTRUCTURE: JpaCarRepository
 * 
 * JPA implementation of CarRepository domain interface.
 * This is infrastructure concern - domain layer doesn't know about JPA.
 */
@Repository
public interface JpaCarRepository extends JpaRepository<Car, String>, CarRepository {
    
    @Override
    Optional<Car> findByVin(String vin);
    
    @Override
    List<Car> findByAvailabilityStatus(AvailabilityStatus status);
    
    @Override
    boolean existsByVin(String vin);
}
