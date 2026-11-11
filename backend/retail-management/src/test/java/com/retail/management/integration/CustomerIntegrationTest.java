
package com.retail.management.integration;

import org.springframework.test.web.servlet.request.RequestPostProcessor;
import tools.jackson.databind.ObjectMapper;
import com.retail.management.dto.CustomerRequest;
import com.retail.management.entity.Customer;
import com.retail.management.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CustomerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CustomerRepository customerRepository;

    private static final String TEST_USER = "test-user";

    private static final RequestPostProcessor authenticatedUser =
            user(TEST_USER).roles("STAFF");

    private CustomerRequest createRequest() {
        CustomerRequest request = new CustomerRequest();
        request.setFullName("John Smith");
        request.setEmail("john.integration@gmail.com");
        request.setPhoneNumber("0901234567");
        request.setAddress("Ho Chi Minh City");
        request.setIsActive(true);
        return request;
    }

    @Test
    void createCustomer_ShouldReturn201_WhenValidRequest() throws Exception {
        CustomerRequest request = createRequest();

        mockMvc.perform(post("/api/customers")
                        .with(authenticatedUser)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.fullName").value("John Smith"))
                .andExpect(jsonPath("$.email")
                        .value("john.integration@gmail.com"));
    }

    @Test
    void getAllCustomers_ShouldReturn200() throws Exception {
        Customer customer = new Customer();
        customer.setFullName("John Smith");
        customer.setEmail("john.integration@gmail.com");

        Customer savedCustomer = customerRepository.saveAndFlush(customer);

        mockMvc.perform(get("/api/customers").with(authenticatedUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + savedCustomer.getId()
                        + ")].fullName").value(
                        org.hamcrest.Matchers.contains("John Smith")
                ));
    }

    @Test
    void getCustomerById_ShouldReturn200_WhenCustomerExists() throws Exception {
        Customer customer = new Customer();
        customer.setFullName("John Smith");
        customer.setEmail("john.integration@gmail.com");

        Customer savedCustomer = customerRepository.saveAndFlush(customer);

        mockMvc.perform(get("/api/customers/{id}", savedCustomer.getId()).with(authenticatedUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedCustomer.getId()))
                .andExpect(jsonPath("$.fullName").value("John Smith"));
    }

    @Test
    void updateCustomer_ShouldReturn200_WhenValidRequest() throws Exception {
        Customer customer = new Customer();
        customer.setFullName("John Smith");
        customer.setEmail("john.integration@gmail.com");

        Customer savedCustomer = customerRepository.saveAndFlush(customer);

        CustomerRequest request = createRequest();
        request.setFullName("John Updated");
        request.setEmail("john.updated@gmail.com");

        mockMvc.perform(put("/api/customers/{id}", savedCustomer.getId())
                        .with(authenticatedUser)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedCustomer.getId()))
                .andExpect(jsonPath("$.fullName").value("John Updated"))
                .andExpect(jsonPath("$.email").value("john.updated@gmail.com"));
    }

    @Test
    void deleteCustomer_ShouldReturn204_WhenCustomerExists() throws Exception {
        Customer customer = new Customer();
        customer.setFullName("John Smith");
        customer.setEmail("john.integration@gmail.com");

        Customer savedCustomer = customerRepository.saveAndFlush(customer);

        mockMvc.perform(delete("/api/customers/{id}", savedCustomer.getId()).with(authenticatedUser))
                .andExpect(status().isNoContent());

        org.junit.jupiter.api.Assertions.assertFalse(
                customerRepository.findById(savedCustomer.getId()).isPresent()
        );
    }

    @Test
    void createCustomer_ShouldReturn409_WhenEmailAlreadyExists() throws Exception {
        Customer customer = new Customer();
        customer.setFullName("John Smith");
        customer.setEmail("john.integration@gmail.com");

        customerRepository.saveAndFlush(customer);

        CustomerRequest request = createRequest();

        mockMvc.perform(post("/api/customers")
                        .with(authenticatedUser)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void getCustomerById_ShouldReturn404_WhenCustomerNotFound() throws Exception {
        mockMvc.perform(get("/api/customers/{id}", 999999L).with(authenticatedUser))
                .andExpect(status().isNotFound());
    }

}
