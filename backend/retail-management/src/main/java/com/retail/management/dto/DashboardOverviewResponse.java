
package com.retail.management.dto;

import java.math.BigDecimal;

public record DashboardOverviewResponse(
        Long totalProducts,
        Long totalCustomers,
        Long totalSuppliers,
        Long totalOrders,
        Long completedOrders,
        Long pendingOrders,
        Long cancelledOrders,
        BigDecimal totalRevenue,
        Long lowStockProducts
) {
}
