
package com.retail.management.dto;

import java.math.BigDecimal;
import java.util.Map;

public record OrderStatisticsResponse(
        Long totalOrders,
        Map<String, Long> ordersByStatus,
        BigDecimal totalRevenue
) {
}
