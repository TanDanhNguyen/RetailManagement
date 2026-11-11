
package com.retail.management.integration;

import tools.jackson.databind.ObjectMapper;
import com.retail.management.dto.SupplierRequest;
import com.retail.management.entity.Supplier;
import com.retail.management.repository.SupplierRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SupplierIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SupplierRepository supplierRepository;

    private SupplierRequest createRequest() {
        SupplierRequest request = new SupplierRequest();
        request.setSupplierName("ABC Technology");
        request.setContactPerson("John Smith");
        request.setEmail("abc.integration@gmail.com");
        request.setPhoneNumber("0901234567");
        request.setAddress("Ho Chi Minh City");
        request.setActive(true);
        return request;
    }

    @Test
    void createSupplier_ShouldReturn201_WhenValidRequest()
            throws Exception {

        SupplierRequest request = createRequest();

        mockMvc.perform(post("/api/suppliers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.supplierName")
                        .value("ABC Technology"))
                .andExpect(jsonPath("$.email")
                        .value("abc.integration@gmail.com"));
    }

    @Test
    void getAllSuppliers_ShouldReturn200() throws Exception {
        Supplier supplier = new Supplier();
        supplier.setSupplierName("ABC Technology");
        supplier.setEmail("abc.integration@gmail.com");

        Supplier savedSupplier =
                supplierRepository.saveAndFlush(supplier);

        mockMvc.perform(get("/api/suppliers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(
                        "$[?(@.id == " + savedSupplier.getId()
                                + ")].supplierName"
                ).value(org.hamcrest.Matchers.contains("ABC Technology")));
    }

    @Test
    void getSupplierById_ShouldReturn200_WhenExists()
            throws Exception {

        Supplier supplier = new Supplier();
        supplier.setSupplierName("ABC Technology");
        supplier.setEmail("abc.integration@gmail.com");

        Supplier savedSupplier =
                supplierRepository.saveAndFlush(supplier);

        mockMvc.perform(get(
                        "/api/suppliers/{id}",
                        savedSupplier.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(savedSupplier.getId()))
                .andExpect(jsonPath("$.supplierName")
                        .value("ABC Technology"));
    }

    @Test
    void updateSupplier_ShouldReturn200_WhenValidRequest()
            throws Exception {

        Supplier supplier = new Supplier();
        supplier.setSupplierName("ABC Technology");
        supplier.setEmail("abc.integration@gmail.com");

        Supplier savedSupplier =
                supplierRepository.saveAndFlush(supplier);

        SupplierRequest request = createRequest();
        request.setSupplierName("ABC Updated");
        request.setEmail("abc.updated@gmail.com");

        mockMvc.perform(put(
                        "/api/suppliers/{id}",
                        savedSupplier.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.supplierName")
                        .value("ABC Updated"))
                .andExpect(jsonPath("$.email")
                        .value("abc.updated@gmail.com"));
    }

    @Test
    void deleteSupplier_ShouldReturn204_WhenExists()
            throws Exception {

        Supplier supplier = new Supplier();
        supplier.setSupplierName("ABC Technology");
        supplier.setEmail("abc.integration@gmail.com");

        Supplier savedSupplier =
                supplierRepository.saveAndFlush(supplier);

        mockMvc.perform(delete(
                        "/api/suppliers/{id}",
                        savedSupplier.getId()))
                .andExpect(status().isNoContent());

        assertFalse(
                supplierRepository.findById(savedSupplier.getId())
                        .isPresent()
        );
    }

    @Test
    void createSupplier_ShouldReturn409_WhenEmailExists()
            throws Exception {

        Supplier supplier = new Supplier();
        supplier.setSupplierName("ABC Technology");
        supplier.setEmail("abc.integration@gmail.com");

        supplierRepository.saveAndFlush(supplier);

        SupplierRequest request = createRequest();

        mockMvc.perform(post("/api/suppliers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void getSupplierById_ShouldReturn404_WhenNotFound()
            throws Exception {

        mockMvc.perform(get("/api/suppliers/{id}", 999999L))
                .andExpect(status().isNotFound());
    }
}
