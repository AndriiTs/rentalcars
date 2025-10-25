package com.rentalcar.customer.application;

import com.rentalcar.customer.domain.model.ContactInfo;
import com.rentalcar.customer.domain.model.Customer;
import com.rentalcar.customer.domain.model.LicenseInfo;
import com.rentalcar.customer.domain.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * APPLICATION SERVICE: CustomerService
 * 
 * Coordinates customer management operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {
    
    private final CustomerRepository customerRepository;
    
    /**
     * Register a new customer
     */
    @Transactional
    public Customer registerCustomer(String firstName, String lastName, LocalDate dateOfBirth,
                                    String email, String phone,
                                    String licenseNumber, String issuingCountry, LocalDate licenseExpiration) {
        
        log.info("Registering new customer: {} {}", firstName, lastName);
        
        // Check if customer already exists
        if (customerRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Customer with email " + email + " already exists");
        }
        
        // Create value objects
        ContactInfo contactInfo = ContactInfo.of(email, phone);
        LicenseInfo licenseInfo = LicenseInfo.of(licenseNumber, issuingCountry, licenseExpiration);
        
        // Create customer aggregate
        Customer customer = Customer.create(firstName, lastName, dateOfBirth, contactInfo, licenseInfo);
        customer = customerRepository.save(customer);
        
        log.info("Customer registered successfully: {}", customer.getCustomerId());
        return customer;
    }
    
    /**
     * Verify customer
     */
    @Transactional
    public void verifyCustomer(String customerId) {
        log.info("Verifying customer: {}", customerId);
        
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + customerId));
        
        customer.verify();
        customerRepository.save(customer);
        
        log.info("Customer verified successfully: {}", customerId);
    }
    
    /**
     * Get all verified customers
     */
    @Transactional(readOnly = true)
    public List<Customer> getVerifiedCustomers() {
        return customerRepository.findVerifiedCustomers();
    }
    
    /**
     * Get all customers
     */
    @Transactional(readOnly = true)
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }
}
