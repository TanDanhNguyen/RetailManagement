
package com.retail.management.service;

import com.retail.management.dto.*;
import com.retail.management.entity.*;
import com.retail.management.exception.ReportNotFoundException;
import com.retail.management.repository.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    private static final int DEFAULT_LOW_STOCK_THRESHOLD = 5;

    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final SupplierRepository supplierRepository;
    private final OrderRepository orderRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final DailySalesReportRepository dailySalesReportRepository;

    public DashboardService(
            ProductRepository productRepository,
            CustomerRepository customerRepository,
            SupplierRepository supplierRepository,
            OrderRepository orderRepository,
            InventoryTransactionRepository inventoryTransactionRepository,
            DailySalesReportRepository dailySalesReportRepository
    ) {
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.supplierRepository = supplierRepository;
        this.orderRepository = orderRepository;
        this.inventoryTransactionRepository = inventoryTransactionRepository;
        this.dailySalesReportRepository = dailySalesReportRepository;
    }

    @Transactional(readOnly = true)
    public DashboardOverviewResponse getOverview() {

        List<Product> products = productRepository.findAll();
        List<Order> orders = orderRepository.findAll();

        long completedOrders = orders.stream()
                .filter(order -> order.getStatus() == OrderStatus.COMPLETED)
                .count();

        long pendingOrders = orders.stream()
                .filter(order -> order.getStatus() == OrderStatus.PENDING)
                .count();

        long cancelledOrders = orders.stream()
                .filter(order -> order.getStatus() == OrderStatus.CANCELLED)
                .count();

        BigDecimal totalRevenue = calculateRevenue(orders);

        long lowStockProducts = countLowStockProducts(
                products,
                DEFAULT_LOW_STOCK_THRESHOLD
        );

        return new DashboardOverviewResponse(
                (long) products.size(),
                customerRepository.count(),
                supplierRepository.count(),
                (long) orders.size(),
                completedOrders,
                pendingOrders,
                cancelledOrders,
                totalRevenue,
                lowStockProducts
        );
    }


    @Transactional(readOnly = true)
    public OrderStatisticsResponse getOrderStatistics() {

        List<Order> orders = orderRepository.findAll();

        Map<String, Long> ordersByStatus = new java.util.LinkedHashMap<>();

        // Initialize all statuses with 0
        for (OrderStatus status : OrderStatus.values()) {
            ordersByStatus.put(status.name(), 0L);
        }

        // Group orders by status and count
        orders.stream()
                .collect(
                        java.util.stream.Collectors.groupingBy(
                                Order::getStatus,
                                java.util.stream.Collectors.counting()
                        )
                )
                .forEach((status, count) ->
                        ordersByStatus.put(status.name(), count)
                );

        return new OrderStatisticsResponse(
                (long) orders.size(),
                ordersByStatus,
                calculateRevenue(orders)
        );
    }

    @Transactional(readOnly = true)
    public InventorySummaryResponse getInventorySummary() {

        List<Product> products = productRepository.findAll();

        List<InventoryTransaction> transactions =
                inventoryTransactionRepository.findAll();

        long stockIn = transactions.stream()
                .filter(t -> t.getType() ==
                        InventoryTransactionType.STOCK_IN)
                .count();

        long stockOut = transactions.stream()
                .filter(t -> t.getType() ==
                        InventoryTransactionType.STOCK_OUT)
                .count();

        long adjustment = transactions.stream()
                .filter(t -> t.getType() ==
                        InventoryTransactionType.ADJUSTMENT)
                .count();

        long totalStock = products.stream()
                .mapToLong(Product::getStockQuantity)
                .sum();

        long lowStock = countLowStockProducts(
                products,
                DEFAULT_LOW_STOCK_THRESHOLD
        );

        return new InventorySummaryResponse(
                (long) transactions.size(),
                stockIn,
                stockOut,
                adjustment,
                totalStock,
                lowStock
        );
    }

    @Transactional(readOnly = true)
    public List<LowStockProductResponse> getLowStockProducts(
            Integer threshold
    ) {
        int actualThreshold = threshold == null
                ? DEFAULT_LOW_STOCK_THRESHOLD
                : threshold;

        if (actualThreshold < 0) {
            throw new IllegalArgumentException(
                    "Threshold cannot be negative"
            );
        }

        return productRepository.findAll()
                .stream()
                .filter(product ->
                        product.getStockQuantity() <= actualThreshold
                )
                .map(product -> new LowStockProductResponse(
                        product.getId(),
                        product.getName(),
                        product.getSku(),
                        product.getStockQuantity(),
                        actualThreshold
                ))
                .toList();
    }

    @Transactional
    public DailySalesReportResponse generateDailyReport(
            LocalDate reportDate
    ) {
        List<Order> dailyOrders = orderRepository.findAll()
                .stream()
                .filter(order ->
                        order.getCreatedAt() != null
                                && order.getCreatedAt().toLocalDate()
                                .equals(reportDate)
                )
                .toList();

        long totalOrders = dailyOrders.size();

        long completedOrders = dailyOrders.stream()
                .filter(order ->
                        order.getStatus() == OrderStatus.COMPLETED
                )
                .count();

        long cancelledOrders = dailyOrders.stream()
                .filter(order ->
                        order.getStatus() == OrderStatus.CANCELLED
                )
                .count();

        BigDecimal revenue = calculateRevenue(dailyOrders);

        DailySalesReport report = dailySalesReportRepository
                .findByReportDate(reportDate)
                .orElseGet(DailySalesReport::new);

        report.setReportDate(reportDate);
        report.setTotalOrders(totalOrders);
        report.setCompletedOrders(completedOrders);
        report.setCancelledOrders(cancelledOrders);
        report.setTotalRevenue(revenue);
        report.setGeneratedAt(java.time.LocalDateTime.now());

        return mapToDailyReportResponse(
                dailySalesReportRepository.save(report)
        );
    }

    @Transactional(readOnly = true)
    public DailySalesReportResponse getDailyReport(
            LocalDate reportDate
    ) {
        DailySalesReport report = dailySalesReportRepository
                .findByReportDate(reportDate)
                .orElseThrow(() -> new ReportNotFoundException(
                        "Daily report not found: " + reportDate
                ));

        return mapToDailyReportResponse(report);
    }

    @Transactional(readOnly = true)
    public List<DailySalesReportResponse> getAllDailyReports() {

        return dailySalesReportRepository
                .findAllByOrderByReportDateDesc()
                .stream()
                .map(this::mapToDailyReportResponse)
                .toList();
    }

    private BigDecimal calculateRevenue(List<Order> orders) {

        return orders.stream()
                .filter(order ->
                        order.getStatus() == OrderStatus.COMPLETED
                )
                .map(order -> order.getTotalAmount() == null
                        ? BigDecimal.ZERO
                        : new BigDecimal(order.getTotalAmount().toString())
                )
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private long countLowStockProducts(
            List<Product> products,
            int threshold
    ) {
        return products.stream()
                .filter(product ->
                        product.getStockQuantity() <= threshold
                )
                .count();
    }

    private DailySalesReportResponse mapToDailyReportResponse(
            DailySalesReport report
    ) {
        return new DailySalesReportResponse(
                report.getId(),
                report.getReportDate(),
                report.getTotalOrders(),
                report.getCompletedOrders(),
                report.getCancelledOrders(),
                report.getTotalRevenue(),
                report.getGeneratedAt()
        );
    }
}
