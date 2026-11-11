
package com.retail.management.dto;

import com.retail.management.entity.InventoryTransactionType;
import java.time.LocalDateTime;

public record InventoryTransactionResponse(
        Long id,
        Long productId,
        String productName,
        Long supplierId,
        String supplierName,
        InventoryTransactionType type,
        Integer quantity,
        Integer stockBefore,
        Integer stockAfter,
        String reference,
        String notes,
        LocalDateTime createdAt
) {
}
