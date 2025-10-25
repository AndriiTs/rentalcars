package com.rentalcar.fleet.infrastructure.web;

import com.rentalcar.fleet.application.FleetService;
import com.rentalcar.fleet.domain.model.Car;
import com.rentalcar.fleet.infrastructure.web.dto.AddCarRequest;
import com.rentalcar.fleet.infrastructure.web.dto.CarResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * INFRASTRUCTURE: REST Controller for Fleet operations
 *
 * This is infrastructure/interface layer - not domain layer.
 * Controllers translate HTTP requests to domain operations.
 */
@Slf4j
@RestController
@RequestMapping("/api/fleet/cars")
@RequiredArgsConstructor
public class FleetController {

    private final FleetService fleetService;

    /**
     * Add a new car to the fleet
     * POST /api/fleet/cars
     */
    @PostMapping
    public ResponseEntity<CarResponse> addCar(@RequestBody AddCarRequest request) {
        log.info("Received add car request: VIN={}, License={}", request.vin(), request.licensePlate());

        try {
            Car car = fleetService.addCar(
                    request.vin(),
                    request.licensePlate(),
                    request.make(),
                    request.model(),
                    request.year(),
                    request.category(),
                    request.dailyRate(),
                    request.currency()
            );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(CarResponse.fromDomain(car));

        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("Error adding car: {}", e.getMessage());
            return ResponseEntity
                    .badRequest()
                    .build();
        }
    }

    /**
     * Get all cars
     * GET /api/fleet/cars
     */
    @GetMapping
    public ResponseEntity<List<CarResponse>> getAllCars() {
        List<Car> cars = fleetService.getAllCars();
        List<CarResponse> responses = cars.stream()
                .map(CarResponse::fromDomain)
                .toList();

        return ResponseEntity.ok(responses);
    }

    /**
     * Get all available cars
     * GET /api/fleet/cars/available
     */
    @GetMapping("/available")
    public ResponseEntity<List<CarResponse>> getAvailableCars() {
        List<Car> cars = fleetService.getAvailableCars();
        List<CarResponse> responses = cars.stream()
                .map(CarResponse::fromDomain)
                .toList();

        return ResponseEntity.ok(responses);
    }

    /**
     * Send car to maintenance
     * POST /api/fleet/cars/{carId}/maintenance
     */
    @PostMapping("/{carId}/maintenance")
    public ResponseEntity<Void> sendToMaintenance(@PathVariable String carId) {
        log.info("Sending car to maintenance: {}", carId);

        try {
            fleetService.sendToMaintenance(carId);
            return ResponseEntity.ok().build();

        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("Error sending car to maintenance: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
