
package com.retail.management.service;

import com.retail.management.dto.*;
import com.retail.management.entity.*;
import com.retail.management.exception.ReportNotFoundException;
import com.retail.management.repository.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private InventoryTransactionRepository inventoryTransactionRepository;

    @Mock
    private DailySalesReportRepository dailySalesReportRepository;

    @InjectMocks
    private DashboardService dashboardService;

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setName("Laptop");
        product.setSku("LAP-001");
        product.setStockQuantity(3);
        product.setIsActive(true);
    }

    private Order createOrder(
            OrderStatus status,
            double totalAmount,
            LocalDateTime createdAt
    ) {
        Order order = new Order();
        order.setStatus(status);
        order.setTotalAmount(BigDecimal.valueOf(totalAmount));
        order.setCreatedAt(createdAt);
        return order;
    }

    @Test
    void getOverview_ShouldReturnDashboardSummary() {

        List<Product> products = List.of(product);

        List<Order> orders = List.of(
                createOrder(
                        OrderStatus.COMPLETED,
                        100.0,
                        LocalDateTime.now()
                ),
                createOrder(
                        OrderStatus.PENDING,
                        50.0,
                        LocalDateTime.now()
                ),
                createOrder(
                        OrderStatus.CANCELLED,
                        30.0,
                        LocalDateTime.now()
                )
        );

        when(productRepository.findAll()).thenReturn(products);
        when(orderRepository.findAll()).thenReturn(orders);
        when(customerRepository.count()).thenReturn(10L);
        when(supplierRepository.count()).thenReturn(5L);

        DashboardOverviewResponse response =
                dashboardService.getOverview();

        assertEquals(1L, response.totalProducts());
        assertEquals(10L, response.totalCustomers());
        assertEquals(5L, response.totalSuppliers());
        assertEquals(3L, response.totalOrders());
        assertEquals(1L, response.completedOrders());
        assertEquals(1L, response.pendingOrders());
        assertEquals(1L, response.cancelledOrders());
        assertEquals(
                new BigDecimal("100.0"),
                response.totalRevenue()
        );
        assertEquals(1L, response.lowStockProducts());
    }

    @Test
    void getOrderStatistics_ShouldCountOrdersByStatus() {

        when(orderRepository.findAll()).thenReturn(List.of(
                createOrder(
                        OrderStatus.COMPLETED, 100.0, LocalDateTime.now()
                ),
                createOrder(
                        OrderStatus.COMPLETED, 200.0, LocalDateTime.now()
                ),
                createOrder(
                        OrderStatus.PENDING, 50.0, LocalDateTime.now()
                )
        ));

        OrderStatisticsResponse response =
                dashboardService.getOrderStatistics();

        assertEquals(3L, response.totalOrders());
        assertEquals(2L, response.ordersByStatus().get("COMPLETED"));
        assertEquals(1L, response.ordersByStatus().get("PENDING"));
        assertEquals(0L, response.ordersByStatus().get("CANCELLED"));
        assertEquals(
                new BigDecimal("300.0"),
                response.totalRevenue()
        );
    }

    @Test
    void getInventorySummary_ShouldReturnInventoryStatistics() {

        InventoryTransaction stockIn = createTransaction(
                InventoryTransactionType.STOCK_IN
        );

        InventoryTransaction stockOut = createTransaction(
                InventoryTransactionType.STOCK_OUT
        );

        InventoryTransaction adjustment = createTransaction(
                InventoryTransactionType.ADJUSTMENT
        );

        when(productRepository.findAll()).thenReturn(List.of(product));
        when(inventoryTransactionRepository.findAll())
                .thenReturn(List.of(stockIn, stockOut, adjustment));

        InventorySummaryResponse response =
                dashboardService.getInventorySummary();

        assertEquals(3L, response.totalTransactions());
        assertEquals(1L, response.stockInTransactions());
        assertEquals(1L, response.stockOutTransactions());
        assertEquals(1L, response.adjustmentTransactions());
        assertEquals(3L, response.totalStockQuantity());
        assertEquals(1L, response.lowStockProducts());
    }

    @Test
    void getLowStockProducts_ShouldReturnProductsBelowThreshold() {

        Product product2 = new Product();
        product2.setId(2L);
        product2.setName("Mouse");
        product2.setSku("MOU-001");
        product2.setStockQuantity(20);

        when(productRepository.findAll())
                .thenReturn(List.of(product, product2));

        List<LowStockProductResponse> result =
                dashboardService.getLowStockProducts(5);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).productId());
        assertEquals("Laptop", result.get(0).productName());
        assertEquals(3, result.get(0).stockQuantity());
        assertEquals(5, result.get(0).threshold());
    }

    @Test
    void getLowStockProducts_ShouldThrowException_WhenThresholdNegative() {

        assertThrows(
                IllegalArgumentException.class,
                () -> dashboardService.getLowStockProducts(-1)
        );

        verify(productRepository, never()).findAll();
    }

    @Test
    void generateDailyReport_ShouldSaveReport() {

        LocalDate reportDate = LocalDate.of(2026, 11, 11);

        when(orderRepository.findAll()).thenReturn(List.of(
                createOrder(
                        OrderStatus.COMPLETED,
                        100.0,
                        reportDate.atTime(10, 0)
                ),
                createOrder(
                        OrderStatus.CANCELLED,
                        50.0,
                        reportDate.atTime(11, 0)
                ),
                createOrder(
                        OrderStatus.PENDING,
                        30.0,
                        reportDate.atTime(12, 0)
                )
        ));

        when(dailySalesReportRepository.findByReportDate(reportDate))
                .thenReturn(Optional.empty());

        when(dailySalesReportRepository.save(any(DailySalesReport.class)))
                .thenAnswer(invocation -> {
                    DailySalesReport report = invocation.getArgument(0);
                    return report;
                });

        DailySalesReportResponse response =
                dashboardService.generateDailyReport(reportDate);

        assertEquals(reportDate, response.reportDate());
        assertEquals(3L, response.totalOrders());
        assertEquals(1L, response.completedOrders());
        assertEquals(1L, response.cancelledOrders());
        assertEquals(
                new BigDecimal("100.0"),
                response.totalRevenue()
        );

        verify(dailySalesReportRepository).save(any(DailySalesReport.class));
    }

    @Test
    void generateDailyReport_ShouldUpdateExistingReport() {

        LocalDate reportDate = LocalDate.of(2026, 11, 11);

        DailySalesReport existing = new DailySalesReport();
        existing.setReportDate(reportDate);
        existing.setTotalOrders(1L);
        existing.setCompletedOrders(1L);
        existing.setCancelledOrders(0L);
        existing.setTotalRevenue(new BigDecimal("50.00"));

        when(orderRepository.findAll()).thenReturn(List.of(
                createOrder(
                        OrderStatus.COMPLETED,
                        150.0,
                        reportDate.atTime(10, 0)
                )
        ));

        when(dailySalesReportRepository.findByReportDate(reportDate))
                .thenReturn(Optional.of(existing));

        when(dailySalesReportRepository.save(any(DailySalesReport.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DailySalesReportResponse response =
                dashboardService.generateDailyReport(reportDate);

        assertEquals(1L, response.totalOrders());
        assertEquals(
                new BigDecimal("150.0"),
                response.totalRevenue()
        );

        verify(dailySalesReportRepository, times(1))
                .save(existing);
    }

    @Test
    void getDailyReport_ShouldReturnReport_WhenExists() {

        LocalDate reportDate = LocalDate.of(2026, 11, 11);

        DailySalesReport report = new DailySalesReport();
        report.setReportDate(reportDate);
        report.setTotalOrders(2L);
        report.setCompletedOrders(1L);
        report.setCancelledOrders(0L);
        report.setTotalRevenue(new BigDecimal("100.00"));

        when(dailySalesReportRepository.findByReportDate(reportDate))
                .thenReturn(Optional.of(report));

        DailySalesReportResponse response =
                dashboardService.getDailyReport(reportDate);

        assertEquals(reportDate, response.reportDate());
        assertEquals(2L, response.totalOrders());
    }

    @Test
    void getDailyReport_ShouldThrowException_WhenNotFound() {

        LocalDate reportDate = LocalDate.of(2026, 11, 11);

        when(dailySalesReportRepository.findByReportDate(reportDate))
                .thenReturn(Optional.empty());

        assertThrows(
                ReportNotFoundException.class,
                () -> dashboardService.getDailyReport(reportDate)
        );
    }

    private InventoryTransaction createTransaction(
            InventoryTransactionType type
    ) {
        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setProduct(product);
        transaction.setType(type);
        transaction.setQuantity(5);
        transaction.setStockBefore(10);
        transaction.setStockAfter(15);
        return transaction;
    }
}
