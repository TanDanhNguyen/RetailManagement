
package com.retail.management.controller;

import com.retail.management.dto.*;
import com.retail.management.service.DashboardService;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/overview")
    public DashboardOverviewResponse getOverview() {
        return dashboardService.getOverview();
    }

    @GetMapping("/order-statistics")
    public OrderStatisticsResponse getOrderStatistics() {
        return dashboardService.getOrderStatistics();
    }

    @GetMapping("/inventory-summary")
    public InventorySummaryResponse getInventorySummary() {
        return dashboardService.getInventorySummary();
    }

    @GetMapping("/low-stock")
    public List<LowStockProductResponse> getLowStockProducts(
            @RequestParam(defaultValue = "5") Integer threshold
    ) {
        return dashboardService.getLowStockProducts(threshold);
    }
}
