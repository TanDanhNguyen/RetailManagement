
package com.retail.management.service;

import com.retail.management.dto.CustomerRequest;
import com.retail.management.dto.CustomerResponse;
import com.retail.management.entity.Customer;
import com.retail.management.exception.DuplicateCustomerException;
import com.retail.management.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    private CustomerRequest createRequest() {
        CustomerRequest request = new CustomerRequest();
        request.setFullName("John Smith");
        request.setEmail("john@gmail.com");
        request.setPhoneNumber("0901234567");
        request.setAddress("Ho Chi Minh City");
        request.setIsActive(true);
        return request;
    }

    @Test
    void createCustomer_ShouldReturnCustomer_WhenValidRequest() {
        CustomerRequest request = createRequest();

        when(customerRepository.existsByEmail(request.getEmail()))
                .thenReturn(false);

        when(customerRepository.save(any(Customer.class)))
                .thenAnswer(invocation -> {
                    Customer customer = invocation.getArgument(0);
                    customer.setId(1L);
                    return customer;
                });

        CustomerResponse response =
                customerService.createCustomer(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("John Smith", response.getFullName());
        assertEquals("john@gmail.com", response.getEmail());

        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void createCustomer_ShouldThrowException_WhenEmailAlreadyExists() {
        CustomerRequest request = createRequest();

        when(customerRepository.existsByEmail(request.getEmail()))
                .thenReturn(true);

        assertThrows(
                DuplicateCustomerException.class,
                () -> customerService.createCustomer(request)
        );

        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void getAllCustomers_ShouldReturnCustomerList() {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setFullName("John Smith");
        customer.setEmail("john@gmail.com");

        when(customerRepository.findAll())
                .thenReturn(java.util.List.of(customer));

        var result = customerService.getAllCustomers();

        assertEquals(1, result.size());
        assertEquals("John Smith", result.get(0).getFullName());
    }

    @Test
    void getCustomerById_ShouldReturnCustomer_WhenCustomerExists() {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setFullName("John Smith");
        customer.setEmail("john@gmail.com");

        when(customerRepository.findById(1L))
                .thenReturn(java.util.Optional.of(customer));

        CustomerResponse response = customerService.getCustomerById(1L);

        assertEquals(1L, response.getId());
        assertEquals("John Smith", response.getFullName());
    }

    @Test
    void getCustomerById_ShouldThrowException_WhenCustomerNotFound() {
        when(customerRepository.findById(999L))
                .thenReturn(java.util.Optional.empty());

        assertThrows(
                com.retail.management.exception.ResourceNotFoundException.class,
                () -> customerService.getCustomerById(999L)
        );
    }

    @Test
    void updateCustomer_ShouldReturnUpdatedCustomer_WhenValidRequest() {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setFullName("John Smith");
        customer.setEmail("john@gmail.com");

        CustomerRequest request = createRequest();
        request.setFullName("John Updated");

        when(customerRepository.findById(1L))
                .thenReturn(java.util.Optional.of(customer));

        when(customerRepository.findByEmail(request.getEmail()))
                .thenReturn(java.util.Optional.of(customer));

        when(customerRepository.save(any(Customer.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CustomerResponse response =
                customerService.updateCustomer(1L, request);

        assertEquals("John Updated", response.getFullName());
        assertEquals("john@gmail.com", response.getEmail());
    }

    @Test
    void updateCustomer_ShouldThrowException_WhenEmailBelongsToAnotherCustomer() {
        Customer customer = new Customer();
        customer.setId(1L);

        Customer anotherCustomer = new Customer();
        anotherCustomer.setId(2L);
        anotherCustomer.setEmail("john@gmail.com");

        CustomerRequest request = createRequest();

        when(customerRepository.findById(1L))
                .thenReturn(java.util.Optional.of(customer));

        when(customerRepository.findByEmail(request.getEmail()))
                .thenReturn(java.util.Optional.of(anotherCustomer));

        assertThrows(
                DuplicateCustomerException.class,
                () -> customerService.updateCustomer(1L, request)
        );

        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void deleteCustomer_ShouldDeleteCustomer_WhenCustomerExists() {
        Customer customer = new Customer();
        customer.setId(1L);

        when(customerRepository.findById(1L))
                .thenReturn(java.util.Optional.of(customer));

        customerService.deleteCustomer(1L);

        verify(customerRepository).delete(customer);
    }

    @Test
    void deleteCustomer_ShouldThrowException_WhenCustomerNotFound() {
        when(customerRepository.findById(999L))
                .thenReturn(java.util.Optional.empty());

        assertThrows(
                com.retail.management.exception.ResourceNotFoundException.class,
                () -> customerService.deleteCustomer(999L)
        );

        verify(customerRepository, never()).delete(any(Customer.class));
    }

}
