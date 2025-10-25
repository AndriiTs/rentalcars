package com.rentalcar.customer.infrastructure.web;

import com.rentalcar.customer.application.CustomerService;
import com.rentalcar.customer.domain.model.Customer;
import com.rentalcar.customer.infrastructure.web.dto.CustomerResponse;
import com.rentalcar.customer.infrastructure.web.dto.RegisterCustomerRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * INFRASTRUCTURE: REST Controller for Customer operations
 *
 * This is infrastructure/interface layer - not domain layer.
 * Controllers translate HTTP requests to domain operations.
 */
@Slf4j
@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    /**
     * Register a new customer
     * POST /api/customers
     */
    @PostMapping
    public ResponseEntity<CustomerResponse> registerCustomer(@RequestBody RegisterCustomerRequest request) {
        log.info("Received register customer request: {} {}", request.firstName(), request.lastName());

        try {
            Customer customer = customerService.registerCustomer(
                    request.firstName(),
                    request.lastName(),
                    request.dateOfBirth(),
                    request.email(),
                    request.phone(),
                    request.licenseNumber(),
                    request.issuingCountry(),
                    request.licenseExpiration()
            );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(CustomerResponse.fromDomain(customer));

        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("Error registering customer: {}", e.getMessage());
            return ResponseEntity
                    .badRequest()
                    .build();
        }
    }

    /**
     * Verify a customer
     * POST /api/customers/{customerId}/verify
     */
    @PostMapping("/{customerId}/verify")
    public ResponseEntity<Void> verifyCustomer(@PathVariable String customerId) {
        log.info("Verifying customer: {}", customerId);

        try {
            customerService.verifyCustomer(customerId);
            return ResponseEntity.ok().build();

        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("Error verifying customer: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get all customers
     * GET /api/customers
     */
    @GetMapping
    public ResponseEntity<List<CustomerResponse>> getAllCustomers() {
        List<Customer> customers = customerService.getAllCustomers();
        List<CustomerResponse> responses = customers.stream()
                .map(CustomerResponse::fromDomain)
                .toList();

        return ResponseEntity.ok(responses);
    }

    /**
     * Get all verified customers
     * GET /api/customers/verified
     */
    @GetMapping("/verified")
    public ResponseEntity<List<CustomerResponse>> getVerifiedCustomers() {
        List<Customer> customers = customerService.getVerifiedCustomers();
        List<CustomerResponse> responses = customers.stream()
                .map(CustomerResponse::fromDomain)
                .toList();

        return ResponseEntity.ok(responses);
    }
}
