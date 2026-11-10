
package com.retail.management.service;

import com.retail.management.dto.ProductRequest;
import com.retail.management.dto.ProductResponse;
import com.retail.management.entity.Product;
import com.retail.management.exception.DuplicateResourceException;
import com.retail.management.exception.ResourceNotFoundException;
import com.retail.management.repository.ProductRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private ProductRequest createRequest() {
        ProductRequest request = new ProductRequest();
        request.setName("Laptop Dell");
        request.setSku("DELL-001");
        request.setPrice(new BigDecimal("1500.00"));
        request.setStockQuantity(10);
        request.setIsActive(true);
        request.setVersion(0L);
        return request;
    }

    @Test
    void createProduct_ShouldReturnProduct_WhenValidRequest() {

        ProductRequest request = createRequest();

        when(productRepository.existsBySku("DELL-001"))
                .thenReturn(false);

        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProductResponse response = productService.createProduct(request);

        assertNotNull(response);
        assertEquals("Laptop Dell", response.getName());
        assertEquals("DELL-001", response.getSku());
        assertEquals(new BigDecimal("1500.00"), response.getPrice());

        verify(productRepository).save(any(Product.class));
    }

    @Test
    void createProduct_ShouldThrowException_WhenSkuAlreadyExists() {

        ProductRequest request = createRequest();

        when(productRepository.existsBySku("DELL-001"))
                .thenReturn(true);

        assertThrows(
                DuplicateResourceException.class,
                () -> productService.createProduct(request)
        );

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void getProductById_ShouldReturnProduct_WhenProductExists() {

        Product product = new Product();
        product.setId(1L);
        product.setName("Laptop Dell");
        product.setSku("DELL-001");
        product.setPrice(new BigDecimal("1500.00"));
        product.setStockQuantity(10);
        product.setIsActive(true);

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        ProductResponse response = productService.getProductById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Laptop Dell", response.getName());
    }

    @Test
    void getProductById_ShouldThrowException_WhenProductNotFound() {

        when(productRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> productService.getProductById(999L)
        );
    }

    @Test
    void updateProduct_ShouldReturnUpdatedProduct_WhenValidRequest() {

        Product product = new Product();
        product.setId(1L);
        product.setName("Old Laptop");
        product.setSku("DELL-001");
        product.setVersion(0L);

        ProductRequest request = createRequest();

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(productRepository.findBySku("DELL-001"))
                .thenReturn(Optional.of(product));

        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProductResponse response = productService.updateProduct(1L, request);

        assertEquals("Laptop Dell", response.getName());
        assertEquals("DELL-001", response.getSku());

        verify(productRepository).save(product);
    }

    @Test
    void updateProduct_ShouldThrowException_WhenProductNotFound() {

        ProductRequest request = createRequest();

        when(productRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> productService.updateProduct(999L, request)
        );
    }

    @Test
    void deleteProduct_ShouldDeleteProduct_WhenProductExists() {

        Product product = new Product();
        product.setId(1L);

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        assertDoesNotThrow(() -> productService.deleteProduct(1L));

        verify(productRepository).delete(product);
    }

    @Test
    void deleteProduct_ShouldThrowException_WhenProductNotFound() {

        when(productRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> productService.deleteProduct(999L)
        );

        verify(productRepository, never()).delete(any(Product.class));
    }
    @Test
    void updateProduct_ShouldThrowException_WhenVersionMismatch() {

        Product product = new Product();
        product.setId(1L);
        product.setName("Old Laptop");
        product.setSku("DELL-001");
        product.setVersion(1L);

        ProductRequest request = createRequest();
        request.setVersion(0L);

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        assertThrows(
                ObjectOptimisticLockingFailureException.class,
                () -> productService.updateProduct(1L, request)
        );

        verify(productRepository, never()).save(any(Product.class));
    }
}
