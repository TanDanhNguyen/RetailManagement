
package com.retail.management.controller;

import com.retail.management.dto.InventoryAdjustmentRequest;
import com.retail.management.dto.InventoryRequest;
import com.retail.management.dto.InventoryTransactionResponse;
import com.retail.management.service.InventoryService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping("/stock-in")
    @ResponseStatus(HttpStatus.CREATED)
    public InventoryTransactionResponse stockIn(
            @Valid @RequestBody InventoryRequest request
    ) {
        return inventoryService.stockIn(request);
    }

    @PostMapping("/stock-out")
    @ResponseStatus(HttpStatus.CREATED)
    public InventoryTransactionResponse stockOut(
            @Valid @RequestBody InventoryRequest request
    ) {
        return inventoryService.stockOut(request);
    }

    @PostMapping("/adjustment")
    @ResponseStatus(HttpStatus.CREATED)
    public InventoryTransactionResponse adjustStock(
            @Valid @RequestBody InventoryAdjustmentRequest request
    ) {
        return inventoryService.adjustStock(request);
    }

    @GetMapping("/transactions")
    public List<InventoryTransactionResponse> getAllTransactions() {
        return inventoryService.getAllTransactions();
    }

    @GetMapping("/transactions/{id}")
    public InventoryTransactionResponse getTransactionById(
            @PathVariable Long id
    ) {
        return inventoryService.getTransactionById(id);
    }

    @GetMapping("/products/{productId}/transactions")
    public List<InventoryTransactionResponse> getTransactionsByProduct(
            @PathVariable Long productId
    ) {
        return inventoryService.getTransactionsByProduct(productId);
    }
}
