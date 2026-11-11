
package com.retail.management.controller;

import com.retail.management.dto.DailySalesReportResponse;
import com.retail.management.service.DashboardService;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final DashboardService dashboardService;

    public ReportController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @PostMapping("/daily/{date}")
    @ResponseStatus(HttpStatus.CREATED)
    public DailySalesReportResponse generateDailyReport(
            @PathVariable
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        return dashboardService.generateDailyReport(date);
    }

    @GetMapping("/daily/{date}")
    public DailySalesReportResponse getDailyReport(
            @PathVariable
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        return dashboardService.getDailyReport(date);
    }

    @GetMapping("/daily")
    public List<DailySalesReportResponse> getAllDailyReports() {
        return dashboardService.getAllDailyReports();
    }
}
