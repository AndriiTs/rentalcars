package com.rentalcar.customer.infrastructure.persistence;

import com.rentalcar.customer.domain.model.Customer;
import com.rentalcar.customer.domain.repository.CustomerRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * INFRASTRUCTURE: JpaCustomerRepository
 * 
 * JPA implementation of CustomerRepository domain interface.
 */
@Repository
public interface JpaCustomerRepository extends JpaRepository<Customer, String>, CustomerRepository {
    
    @Query("SELECT c FROM Customer c WHERE c.contactInfo.email = :email")
    Optional<Customer> findByEmail(String email);
    
    @Query("SELECT c FROM Customer c WHERE c.verified = true")
    List<Customer> findVerifiedCustomers();
    
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Customer c WHERE c.contactInfo.email = :email")
    boolean existsByEmail(String email);
}
