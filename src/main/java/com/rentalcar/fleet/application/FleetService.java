package com.rentalcar.fleet.application;

import com.rentalcar.fleet.domain.model.Car;
import com.rentalcar.fleet.domain.model.VehicleCategory;
import com.rentalcar.fleet.domain.model.VehicleSpecification;
import com.rentalcar.fleet.domain.repository.CarRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * APPLICATION SERVICE: FleetService
 * 
 * Coordinates fleet management operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FleetService {
    
    private final CarRepository carRepository;
    
    /**
     * Add a new car to the fleet
     */
    @Transactional
    public Car addCar(String vin, String licensePlate, String make, String model, 
                     Integer year, VehicleCategory category, BigDecimal dailyRate, String currency) {
        
        log.info("Adding new car to fleet: VIN={}, License={}", vin, licensePlate);
        
        // Check if car already exists
        if (carRepository.existsByVin(vin)) {
            throw new IllegalArgumentException("Car with VIN " + vin + " already exists");
        }
        
        // Create value object
        VehicleSpecification specification = VehicleSpecification.of(make, model, year, category);
        
        // Create car aggregate
        Car car = Car.create(vin, licensePlate, specification, dailyRate, currency);
        car = carRepository.save(car);
        
        log.info("Car added successfully: {}", car.getCarId());
        return car;
    }
    
    /**
     * Get all available cars
     */
    @Transactional(readOnly = true)
    public List<Car> getAvailableCars() {
        return carRepository.findAvailableCars();
    }
    
    /**
     * Get all cars in fleet
     */
    @Transactional(readOnly = true)
    public List<Car> getAllCars() {
        return carRepository.findAll();
    }
    
    /**
     * Send car to maintenance
     */
    @Transactional
    public void sendToMaintenance(String carId) {
        log.info("Sending car to maintenance: {}", carId);
        
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new IllegalArgumentException("Car not found: " + carId));
        
        car.sendToMaintenance();
        carRepository.save(car);
        
        log.info("Car sent to maintenance successfully: {}", carId);
    }
}
