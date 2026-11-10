
package com.retail.management.service;

import com.retail.management.dto.ProductRequest;
import com.retail.management.dto.ProductResponse;
import com.retail.management.entity.Product;
import com.retail.management.exception.DuplicateResourceException;
import com.retail.management.exception.ResourceNotFoundException;
import com.retail.management.repository.ProductRepository;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product not found: " + id));

        return mapToResponse(product);
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {

        if (productRepository.existsBySku(request.getSku())) {
            throw new DuplicateResourceException(
                    "Product SKU already exists: " + request.getSku());
        }

        Product product = new Product();
        mapToEntity(request, product);

        Product savedProduct = productRepository.save(product);

        return mapToResponse(savedProduct);
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product not found: " + id));

        boolean skuExists = productRepository.findBySku(request.getSku())
                .filter(existing -> !existing.getId().equals(id))
                .isPresent();

        if (skuExists) {
            throw new RuntimeException(
                    "Product SKU already exists: " + request.getSku());
        }

        if (request.getVersion() == null ||
                !request.getVersion().equals(product.getVersion())) {
            throw new ObjectOptimisticLockingFailureException(
                    Product.class, id);
        }

        mapToEntity(request, product);

        Product updatedProduct = productRepository.save(product);

        return mapToResponse(updatedProduct);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product not found: " + id));

        productRepository.delete(product);
    }

    private void mapToEntity(ProductRequest request, Product product) {
        product.setName(request.getName());
        product.setSku(request.getSku());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setIsActive(
                request.getIsActive() != null
                        ? request.getIsActive()
                        : true
        );
    }

    private ProductResponse mapToResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getSku(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getIsActive(),
                product.getCreatedAt(),
                product.getVersion()
        );
    }
}
