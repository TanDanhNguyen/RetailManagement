
package com.retail.management.service;

import com.retail.management.dto.InventoryAdjustmentRequest;
import com.retail.management.dto.InventoryRequest;
import com.retail.management.dto.InventoryTransactionResponse;
import com.retail.management.entity.*;
import com.retail.management.exception.InsufficientInventoryException;
import com.retail.management.exception.ResourceNotFoundException;
import com.retail.management.repository.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InventoryService {

    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;
    private final InventoryTransactionRepository transactionRepository;

    public InventoryService(
            ProductRepository productRepository,
            SupplierRepository supplierRepository,
            InventoryTransactionRepository transactionRepository
    ) {
        this.productRepository = productRepository;
        this.supplierRepository = supplierRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public InventoryTransactionResponse stockIn(InventoryRequest request) {

        Product product = getLockedProduct(request.productId());

        if (!Boolean.TRUE.equals(product.getIsActive())) {
            throw new IllegalArgumentException("Product is inactive");
        }

        Supplier supplier = null;

        if (request.supplierId() != null) {
            supplier = supplierRepository.findById(request.supplierId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Supplier not found: " + request.supplierId()
                    ));

            if (!Boolean.TRUE.equals(supplier.getActive())) {
                throw new IllegalArgumentException("Supplier is inactive");
            }
        }

        int stockBefore = product.getStockQuantity();
        int stockAfter = stockBefore + request.quantity();

        product.setStockQuantity(stockAfter);
        productRepository.save(product);

        InventoryTransaction transaction = createTransaction(
                product,
                supplier,
                InventoryTransactionType.STOCK_IN,
                request.quantity(),
                stockBefore,
                stockAfter,
                request.reference(),
                request.notes()
        );

        return mapToResponse(transactionRepository.save(transaction));
    }

    @Transactional
    public InventoryTransactionResponse stockOut(InventoryRequest request) {

        if (request.supplierId() != null) {
            throw new IllegalArgumentException(
                    "Supplier ID is not allowed for stock out"
            );
        }

        Product product = getLockedProduct(request.productId());

        if (!Boolean.TRUE.equals(product.getIsActive())) {
            throw new IllegalArgumentException("Product is inactive");
        }

        int stockBefore = product.getStockQuantity();

        if (stockBefore < request.quantity()) {
            throw new InsufficientInventoryException(
                    "Insufficient stock. Available: " + stockBefore
                            + ", requested: " + request.quantity()
            );
        }

        int stockAfter = stockBefore - request.quantity();

        product.setStockQuantity(stockAfter);
        productRepository.save(product);

        InventoryTransaction transaction = createTransaction(
                product,
                null,
                InventoryTransactionType.STOCK_OUT,
                request.quantity(),
                stockBefore,
                stockAfter,
                request.reference(),
                request.notes()
        );

        return mapToResponse(transactionRepository.save(transaction));
    }

    @Transactional
    public InventoryTransactionResponse adjustStock(
            InventoryAdjustmentRequest request
    ) {
        Product product = getLockedProduct(request.productId());

        int stockBefore = product.getStockQuantity();
        int stockAfter = request.newStockQuantity();

        int quantityChanged = Math.abs(stockAfter - stockBefore);

        if (quantityChanged == 0) {
            throw new IllegalArgumentException(
                    "New stock quantity must be different from current stock"
            );
        }

        product.setStockQuantity(stockAfter);
        productRepository.save(product);

        InventoryTransaction transaction = createTransaction(
                product,
                null,
                InventoryTransactionType.ADJUSTMENT,
                quantityChanged,
                stockBefore,
                stockAfter,
                request.reference(),
                request.notes()
        );

        return mapToResponse(transactionRepository.save(transaction));
    }

    @Transactional(readOnly = true)
    public List<InventoryTransactionResponse> getAllTransactions() {
        return transactionRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public InventoryTransactionResponse getTransactionById(Long id) {
        InventoryTransaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inventory transaction not found: " + id
                ));

        return mapToResponse(transaction);
    }

    @Transactional(readOnly = true)
    public List<InventoryTransactionResponse> getTransactionsByProduct(
            Long productId
    ) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException(
                    "Product not found: " + productId
            );
        }

        return transactionRepository
                .findByProduct_IdOrderByCreatedAtDesc(productId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private Product getLockedProduct(Long productId) {
        return productRepository.findByIdForUpdate(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found: " + productId
                ));
    }

    private InventoryTransaction createTransaction(
            Product product,
            Supplier supplier,
            InventoryTransactionType type,
            Integer quantity,
            Integer stockBefore,
            Integer stockAfter,
            String reference,
            String notes
    ) {
        InventoryTransaction transaction = new InventoryTransaction();

        transaction.setProduct(product);
        transaction.setSupplier(supplier);
        transaction.setType(type);
        transaction.setQuantity(quantity);
        transaction.setStockBefore(stockBefore);
        transaction.setStockAfter(stockAfter);
        transaction.setReference(reference);
        transaction.setNotes(notes);

        return transaction;
    }

    private InventoryTransactionResponse mapToResponse(
            InventoryTransaction transaction
    ) {
        Product product = transaction.getProduct();
        Supplier supplier = transaction.getSupplier();

        return new InventoryTransactionResponse(
                transaction.getId(),
                product.getId(),
                product.getName(),
                supplier != null ? supplier.getId() : null,
                supplier != null ? supplier.getSupplierName() : null,
                transaction.getType(),
                transaction.getQuantity(),
                transaction.getStockBefore(),
                transaction.getStockAfter(),
                transaction.getReference(),
                transaction.getNotes(),
                transaction.getCreatedAt()
        );
    }
}
