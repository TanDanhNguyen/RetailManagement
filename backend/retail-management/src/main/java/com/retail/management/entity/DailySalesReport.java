
package com.retail.management.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "daily_sales_reports",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_daily_sales_report_date",
                        columnNames = "report_date"
                )
        }
)
public class DailySalesReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "report_date", nullable = false, unique = true)
    private LocalDate reportDate;

    @Column(nullable = false)
    private Long totalOrders;

    @Column(nullable = false)
    private Long completedOrders;

    @Column(nullable = false)
    private Long cancelledOrders;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalRevenue;

    @Column(nullable = false)
    private LocalDateTime generatedAt;

    @PrePersist
    protected void onCreate() {
        generatedAt = LocalDateTime.now();
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public Long getCancelledOrders() {
        return cancelledOrders;
    }

    public void setCancelledOrders(Long cancelledOrders) {
        this.cancelledOrders = cancelledOrders;
    }

    public Long getCompletedOrders() {
        return completedOrders;
    }

    public void setCompletedOrders(Long completedOrders) {
        this.completedOrders = completedOrders;
    }

    public Long getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(Long totalOrders) {
        this.totalOrders = totalOrders;
    }

    public LocalDate getReportDate() {
        return reportDate;
    }

    public void setReportDate(LocalDate reportDate) {
        this.reportDate = reportDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
