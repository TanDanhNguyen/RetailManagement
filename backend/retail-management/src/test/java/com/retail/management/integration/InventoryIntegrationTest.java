
package com.retail.management.integration;

import org.springframework.test.web.servlet.request.RequestPostProcessor;
import tools.jackson.databind.ObjectMapper;
import com.retail.management.dto.InventoryAdjustmentRequest;
import com.retail.management.dto.InventoryRequest;
import com.retail.management.entity.Product;
import com.retail.management.entity.Supplier;
import com.retail.management.repository.InventoryTransactionRepository;
import com.retail.management.repository.ProductRepository;
import com.retail.management.repository.SupplierRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class InventoryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private InventoryTransactionRepository transactionRepository;

    private Product product;
    private Supplier supplier;

    private static final String TEST_USER = "test-user";

    private static final RequestPostProcessor authenticatedUser =
            user(TEST_USER).roles("STAFF");

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setName("Inventory Test Product");
        product.setSku("INV-" + System.nanoTime());
        product.setPrice(new BigDecimal("100"));
        product.setStockQuantity(10);
        product.setIsActive(true);
        product = productRepository.saveAndFlush(product);

        supplier = new Supplier();
        supplier.setSupplierName("Inventory Test Supplier");
        supplier.setEmail("inventory-" + System.nanoTime() + "@test.com");
        supplier.setActive(true);
        supplier = supplierRepository.saveAndFlush(supplier);
    }

    @Test
    void stockIn_ShouldReturn201AndIncreaseStock() throws Exception {
        InventoryRequest request = new InventoryRequest(
                product.getId(), 5, supplier.getId(), "PO-001", "Import"
        );

        mockMvc.perform(post("/api/inventory/stock-in")
                        .with(authenticatedUser)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("STOCK_IN"))
                .andExpect(jsonPath("$.quantity").value(5))
                .andExpect(jsonPath("$.stockBefore").value(10))
                .andExpect(jsonPath("$.stockAfter").value(15));

        Product updated = productRepository.findById(product.getId())
                .orElseThrow();

        org.junit.jupiter.api.Assertions.assertEquals(
                15, updated.getStockQuantity()
        );
    }

    @Test
    void stockOut_ShouldReturn201AndDecreaseStock() throws Exception {
        InventoryRequest request = new InventoryRequest(
                product.getId(), 3, null, "SO-001", "Export"
        );

        mockMvc.perform(post("/api/inventory/stock-out")
                        .with(authenticatedUser)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("STOCK_OUT"))
                .andExpect(jsonPath("$.stockBefore").value(10))
                .andExpect(jsonPath("$.stockAfter").value(7));

        Product updated = productRepository.findById(product.getId())
                .orElseThrow();

        org.junit.jupiter.api.Assertions.assertEquals(
                7, updated.getStockQuantity()
        );
    }

    @Test
    void stockOut_ShouldReturn409_WhenInsufficientStock() throws Exception {
        InventoryRequest request = new InventoryRequest(
                product.getId(), 20, null, null, null
        );

        mockMvc.perform(post("/api/inventory/stock-out")
                        .with(authenticatedUser)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());

        Product updated = productRepository.findById(product.getId())
                .orElseThrow();

        org.junit.jupiter.api.Assertions.assertEquals(
                10, updated.getStockQuantity()
        );

        org.junit.jupiter.api.Assertions.assertEquals(
                0, transactionRepository.count()
        );
    }

    @Test
    void adjustment_ShouldReturn201AndUpdateStock() throws Exception {
        InventoryAdjustmentRequest request =
                new InventoryAdjustmentRequest(
                        product.getId(), 6, "ADJ-001", "Physical count"
                );

        mockMvc.perform(post("/api/inventory/adjustment")
                        .with(authenticatedUser)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("ADJUSTMENT"))
                .andExpect(jsonPath("$.stockBefore").value(10))
                .andExpect(jsonPath("$.stockAfter").value(6))
                .andExpect(jsonPath("$.quantity").value(4));

        Product updated = productRepository.findById(product.getId())
                .orElseThrow();

        org.junit.jupiter.api.Assertions.assertEquals(
                6, updated.getStockQuantity()
        );
    }

    @Test
    void getTransactions_ShouldReturn200() throws Exception {
        InventoryRequest request = new InventoryRequest(
                product.getId(), 5, null, null, null
        );

        mockMvc.perform(post("/api/inventory/stock-in")
                .with(authenticatedUser)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        mockMvc.perform(get("/api/inventory/transactions").with(authenticatedUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productId")
                        .value(product.getId()));
    }

    @Test
    void getTransactionsByProduct_ShouldReturn200() throws Exception {
        InventoryRequest request = new InventoryRequest(
                product.getId(), 5, null, null, null
        );

        mockMvc.perform(post("/api/inventory/stock-in")
                .with(authenticatedUser)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        mockMvc.perform(get(
                        "/api/inventory/products/{productId}/transactions",
                        product.getId()
                ).with(authenticatedUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].type").value("STOCK_IN"));
    }

    @Test
    void stockIn_ShouldReturn404_WhenProductNotFound() throws Exception {
        InventoryRequest request = new InventoryRequest(
                999999L, 5, null, null, null
        );

        mockMvc.perform(post("/api/inventory/stock-in")
                        .with(authenticatedUser)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void stockIn_ShouldReturn400_WhenQuantityIsInvalid() throws Exception {
        InventoryRequest request = new InventoryRequest(
                product.getId(), 0, null, null, null
        );

        mockMvc.perform(post("/api/inventory/stock-in")
                        .with(authenticatedUser)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
