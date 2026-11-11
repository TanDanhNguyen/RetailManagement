
package com.retail.management.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record InventoryRequest(

        @NotNull(message = "Product ID is required")
        Long productId,

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be greater than 0")
        Integer quantity,

        Long supplierId,

        @Size(max = 100, message = "Reference must not exceed 100 characters")
        String reference,

        @Size(max = 500, message = "Notes must not exceed 500 characters")
        String notes
) {
}
