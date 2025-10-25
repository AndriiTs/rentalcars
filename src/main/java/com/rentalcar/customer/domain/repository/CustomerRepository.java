package com.rentalcar.customer.domain.repository;

import com.rentalcar.customer.domain.model.Customer;

import java.util.List;
import java.util.Optional;

/**
 * REPOSITORY: CustomerRepository
 *
 * Repository interface for Customer aggregate.
 * This is a PURE DOMAIN interface with NO Spring Data dependencies.
 *
 * Implementation is in infrastructure layer (JpaCustomerRepository).
 * This keeps the domain layer framework-independent.
 */
public interface CustomerRepository {
    
    /**
     * Save or update a customer
     */
    Customer save(Customer customer);
    
    /**
     * Find customer by identifier
     */
    Optional<Customer> findById(String customerId);
    
    /**
     * Find customer by email
     */
    Optional<Customer> findByEmail(String email);
    
    /**
     * Find all verified customers
     */
    List<Customer> findVerifiedCustomers();
    
    /**
     * Find all customers
     */
    List<Customer> findAll();
    
    /**
     * Check if customer exists by email
     */
    boolean existsByEmail(String email);
}
