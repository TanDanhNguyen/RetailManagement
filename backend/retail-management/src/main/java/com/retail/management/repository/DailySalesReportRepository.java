
package com.retail.management.repository;

import com.retail.management.entity.DailySalesReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailySalesReportRepository
        extends JpaRepository<DailySalesReport, Long> {

    Optional<DailySalesReport> findByReportDate(LocalDate reportDate);

    List<DailySalesReport> findAllByOrderByReportDateDesc();
}
