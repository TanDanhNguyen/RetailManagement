
package com.retail.management.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record DailySalesReportResponse(
        Long id,
        LocalDate reportDate,
        Long totalOrders,
        Long completedOrders,
        Long cancelledOrders,
        BigDecimal totalRevenue,
        LocalDateTime generatedAt
) {
}
