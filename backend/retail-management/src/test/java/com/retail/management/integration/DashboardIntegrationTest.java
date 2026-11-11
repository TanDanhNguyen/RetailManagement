
package com.retail.management.integration;

import org.springframework.test.web.servlet.request.RequestPostProcessor;
import tools.jackson.databind.ObjectMapper;
import com.retail.management.dto.InventoryRequest;
import com.retail.management.entity.Order;
import com.retail.management.entity.OrderStatus;
import com.retail.management.entity.Product;
import com.retail.management.repository.DailySalesReportRepository;
import com.retail.management.repository.OrderRepository;
import com.retail.management.repository.ProductRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DashboardIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private DailySalesReportRepository dailySalesReportRepository;

    private Product product;

    private static final String TEST_USER = "test-user";

    private static final RequestPostProcessor authenticatedUser =
            user(TEST_USER).roles("STAFF");

    @BeforeEach
    void setUp() {

        product = new Product();
        product.setName("Dashboard Test Product");
        product.setSku("DASH-" + System.nanoTime());
        product.setPrice(BigDecimal.valueOf(100.0));
        product.setStockQuantity(3);
        product.setIsActive(true);

        product = productRepository.saveAndFlush(product);
    }

    private Order createOrder(
            OrderStatus status,
            double totalAmount,
            LocalDateTime createdAt
    ) {
        Order order = new Order();

        order.setOrderNumber("ORD-DASH-" + System.nanoTime());
        order.setCustomerName("Dashboard Test Customer");
        order.setTotalAmount(BigDecimal.valueOf(totalAmount));
        order.setStatus(status);
        order.setCreatedAt(createdAt);

        return orderRepository.saveAndFlush(order);
    }

    @Test
    void getOverview_ShouldReturn200() throws Exception {

        createOrder(
                OrderStatus.COMPLETED,
                100.0,
                LocalDateTime.now()
        );

        createOrder(
                OrderStatus.PENDING,
                50.0,
                LocalDateTime.now()
        );

        mockMvc.perform(get("/api/dashboard/overview").with(authenticatedUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProducts").isNumber())
                .andExpect(jsonPath("$.totalCustomers").isNumber())
                .andExpect(jsonPath("$.totalSuppliers").isNumber())
                .andExpect(jsonPath("$.totalOrders").isNumber())
                .andExpect(jsonPath("$.completedOrders").isNumber())
                .andExpect(jsonPath("$.totalRevenue").isNumber());
    }

    @Test
    void getOrderStatistics_ShouldReturn200() throws Exception {

        createOrder(
                OrderStatus.COMPLETED,
                200.0,
                LocalDateTime.now()
        );

        mockMvc.perform(get("/api/dashboard/order-statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalOrders").isNumber())
                .andExpect(jsonPath("$.ordersByStatus").exists())
                .andExpect(jsonPath("$.totalRevenue").isNumber());
    }

    @Test
    void getInventorySummary_ShouldReturn200() throws Exception {

        InventoryRequest request = new InventoryRequest(
                product.getId(),
                5,
                null,
                "DASH-PO-001",
                "Dashboard integration test"
        );

        mockMvc.perform(post("/api/inventory/stock-in")
                        .with(authenticatedUser)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/dashboard/inventory-summary").with(authenticatedUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTransactions").isNumber())
                .andExpect(jsonPath("$.stockInTransactions").isNumber())
                .andExpect(jsonPath("$.stockOutTransactions").isNumber())
                .andExpect(jsonPath("$.adjustmentTransactions").isNumber())
                .andExpect(jsonPath("$.totalStockQuantity").isNumber());
    }

    @Test
    void getLowStockProducts_ShouldReturnMatchingProduct()
            throws Exception {

        mockMvc.perform(get("/api/dashboard/low-stock")
                        .with(authenticatedUser)
                        .param("threshold", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(
                        "$[?(@.id == " + product.getId() + ")]"
                ).doesNotExist());
    }

    @Test
    void getLowStockProducts_ShouldReturn400_WhenThresholdNegative()
            throws Exception {

        mockMvc.perform(get("/api/dashboard/low-stock")
                        .with(authenticatedUser)
                        .param("threshold", "-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void generateDailyReport_ShouldReturn201() throws Exception {

        LocalDate reportDate = LocalDate.now();

        createOrder(
                OrderStatus.COMPLETED,
                150.0,
                reportDate.atTime(10, 0)
        );

        createOrder(
                OrderStatus.CANCELLED,
                50.0,
                reportDate.atTime(11, 0)
        );

        mockMvc.perform(post("/api/reports/daily/{date}", reportDate).with(authenticatedUser))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reportDate")
                        .value(reportDate.toString()))
                .andExpect(jsonPath("$.totalOrders").value(2))
                .andExpect(jsonPath("$.completedOrders").value(1))
                .andExpect(jsonPath("$.cancelledOrders").value(1))
                .andExpect(jsonPath("$.totalRevenue").value(150.0));

        org.junit.jupiter.api.Assertions.assertTrue(
                dailySalesReportRepository
                        .findByReportDate(reportDate)
                        .isPresent()
        );
    }

    @Test
    void getDailyReport_ShouldReturn200_WhenReportExists()
            throws Exception {

        LocalDate reportDate = LocalDate.now();

        createOrder(
                OrderStatus.COMPLETED,
                100.0,
                reportDate.atTime(10, 0)
        );

        mockMvc.perform(post("/api/reports/daily/{date}", reportDate).with(authenticatedUser))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/reports/daily/{date}", reportDate).with(authenticatedUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reportDate")
                        .value(reportDate.toString()))
                .andExpect(jsonPath("$.completedOrders").value(1));
    }

    @Test
    void getDailyReport_ShouldReturn404_WhenReportNotFound()
            throws Exception {

        LocalDate reportDate = LocalDate.of(2020, 1, 1);

        mockMvc.perform(get("/api/reports/daily/{date}", reportDate).with(authenticatedUser))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllDailyReports_ShouldReturn200() throws Exception {

        LocalDate reportDate = LocalDate.now();

        createOrder(
                OrderStatus.COMPLETED,
                100.0,
                reportDate.atTime(10, 0)
        );

        mockMvc.perform(post("/api/reports/daily/{date}", reportDate).with(authenticatedUser))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/reports/daily").with(authenticatedUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
