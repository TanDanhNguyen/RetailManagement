
package com.retail.management.service;

import com.retail.management.dto.InventoryAdjustmentRequest;
import com.retail.management.dto.InventoryRequest;
import com.retail.management.dto.InventoryTransactionResponse;
import com.retail.management.entity.*;
import com.retail.management.exception.InsufficientInventoryException;
import com.retail.management.repository.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private InventoryTransactionRepository transactionRepository;

    @InjectMocks
    private InventoryService inventoryService;

    private Product product;
    private Supplier supplier;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setName("Laptop");
        product.setStockQuantity(10);
        product.setIsActive(true);

        supplier = new Supplier();
        supplier.setId(1L);
        supplier.setSupplierName("ABC Supplier");
        supplier.setActive(true);
    }

    @Test
    void stockIn_ShouldIncreaseStock_WhenValidRequest() {
        InventoryRequest request =
                new InventoryRequest(1L, 5, 1L, "PO-001", "Import");

        when(productRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(product));
        when(supplierRepository.findById(1L))
                .thenReturn(Optional.of(supplier));
        when(transactionRepository.save(any(InventoryTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        InventoryTransactionResponse response =
                inventoryService.stockIn(request);

        assertEquals(15, product.getStockQuantity());
        assertEquals(InventoryTransactionType.STOCK_IN, response.type());
        assertEquals(10, response.stockBefore());
        assertEquals(15, response.stockAfter());
        assertEquals(5, response.quantity());

        verify(productRepository).save(product);
        verify(transactionRepository).save(any(InventoryTransaction.class));
    }

    @Test
    void stockOut_ShouldDecreaseStock_WhenEnoughStock() {
        InventoryRequest request =
                new InventoryRequest(1L, 3, null, "SO-001", "Export");

        when(productRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(product));
        when(transactionRepository.save(any(InventoryTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        InventoryTransactionResponse response =
                inventoryService.stockOut(request);

        assertEquals(7, product.getStockQuantity());
        assertEquals(InventoryTransactionType.STOCK_OUT, response.type());
        assertEquals(10, response.stockBefore());
        assertEquals(7, response.stockAfter());

        verify(productRepository).save(product);
    }

    @Test
    void stockOut_ShouldThrowException_WhenInsufficientStock() {
        InventoryRequest request =
                new InventoryRequest(1L, 20, null, null, null);

        when(productRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(product));

        assertThrows(
                InsufficientInventoryException.class,
                () -> inventoryService.stockOut(request)
        );

        assertEquals(10, product.getStockQuantity());

        verify(productRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void adjustStock_ShouldUpdateStock_WhenValidRequest() {
        InventoryAdjustmentRequest request =
                new InventoryAdjustmentRequest(
                        1L, 6, "ADJ-001", "Physical count"
                );

        when(productRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(product));
        when(transactionRepository.save(any(InventoryTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        InventoryTransactionResponse response =
                inventoryService.adjustStock(request);

        assertEquals(6, product.getStockQuantity());
        assertEquals(InventoryTransactionType.ADJUSTMENT, response.type());
        assertEquals(10, response.stockBefore());
        assertEquals(6, response.stockAfter());
        assertEquals(4, response.quantity());

        verify(productRepository).save(product);
    }

    @Test
    void adjustStock_ShouldThrowException_WhenStockIsUnchanged() {
        InventoryAdjustmentRequest request =
                new InventoryAdjustmentRequest(
                        1L, 10, null, null
                );

        when(productRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(product));

        assertThrows(
                IllegalArgumentException.class,
                () -> inventoryService.adjustStock(request)
        );

        verify(productRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void getAllTransactions_ShouldReturnTransactions() {
        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setProduct(product);
        transaction.setType(InventoryTransactionType.STOCK_IN);
        transaction.setQuantity(5);
        transaction.setStockBefore(10);
        transaction.setStockAfter(15);

        when(transactionRepository.findAllByOrderByCreatedAtDesc())
                .thenReturn(List.of(transaction));

        List<InventoryTransactionResponse> result =
                inventoryService.getAllTransactions();

        assertEquals(1, result.size());
        assertEquals("Laptop", result.get(0).productName());
    }

    @Test
    void stockIn_ShouldThrowException_WhenSupplierNotFound() {
        InventoryRequest request =
                new InventoryRequest(1L, 5, 999L, null, null);

        when(productRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(product));
        when(supplierRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                com.retail.management.exception.ResourceNotFoundException.class,
                () -> inventoryService.stockIn(request)
        );

        assertEquals(10, product.getStockQuantity());
        verify(productRepository, never()).save(any());
    }
}
