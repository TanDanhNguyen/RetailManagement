package com.retail.management.service;

import com.retail.management.dto.CustomerRequest;
import com.retail.management.dto.CustomerResponse;
import com.retail.management.entity.Customer;
import com.retail.management.exception.DuplicateCustomerException;
import com.retail.management.exception.ResourceNotFoundException;
import com.retail.management.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Transactional
    public CustomerResponse createCustomer(CustomerRequest request) {

        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateCustomerException(
                    "Customer email already exists: " + request.getEmail()
            );
        }

        Customer customer = new Customer();
        mapToEntity(request, customer);

        Customer savedCustomer = customerRepository.save(customer);

        return mapToResponse(savedCustomer);
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> getAllCustomers() {

        return customerRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(Long id) {

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer not found: " + id
                        )
                );

        return mapToResponse(customer);
    }

    @Transactional
    public CustomerResponse updateCustomer(Long id, CustomerRequest request) {

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer not found: " + id
                        )
                );

        boolean emailExists = customerRepository.findByEmail(request.getEmail())
                .filter(existing -> !existing.getId().equals(id))
                .isPresent();

        if (emailExists) {
            throw new DuplicateCustomerException(
                    "Customer email already exists: " + request.getEmail()
            );
        }

        mapToEntity(request, customer);

        Customer updatedCustomer = customerRepository.save(customer);

        return mapToResponse(updatedCustomer);
    }

    @Transactional
    public void deleteCustomer(Long id) {

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer not found: " + id
                        )
                );

        customerRepository.delete(customer);
    }

    private void mapToEntity(CustomerRequest request, Customer customer) {

        customer.setFullName(request.getFullName());
        customer.setEmail(request.getEmail());
        customer.setPhoneNumber(request.getPhoneNumber());
        customer.setAddress(request.getAddress());
        customer.setIsActive(request.getIsActive());
    }

    private CustomerResponse mapToResponse(Customer customer) {

        return new CustomerResponse(
                customer.getId(),
                customer.getFullName(),
                customer.getEmail(),
                customer.getPhoneNumber(),
                customer.getAddress(),
                customer.getIsActive(),
                customer.getCreatedAt()
        );
    }
}