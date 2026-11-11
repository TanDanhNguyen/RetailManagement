
package com.retail.management.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record InventoryAdjustmentRequest(

        @NotNull(message = "Product ID is required")
        Long productId,

        @NotNull(message = "New stock quantity is required")
        @Min(value = 0, message = "Stock quantity cannot be negative")
        Integer newStockQuantity,

        @Size(max = 100, message = "Reference must not exceed 100 characters")
        String reference,

        @Size(max = 500, message = "Notes must not exceed 500 characters")
        String notes
) {
}
