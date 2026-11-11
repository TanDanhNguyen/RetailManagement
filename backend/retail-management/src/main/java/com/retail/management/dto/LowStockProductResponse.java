
package com.retail.management.dto;

public record LowStockProductResponse(
        Long productId,
        String productName,
        String sku,
        Integer stockQuantity,
        Integer threshold
) {
}
