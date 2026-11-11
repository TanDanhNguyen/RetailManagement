
package com.retail.management.dto;

public record InventorySummaryResponse(
        Long totalTransactions,
        Long stockInTransactions,
        Long stockOutTransactions,
        Long adjustmentTransactions,
        Long totalStockQuantity,
        Long lowStockProducts
) {
}
