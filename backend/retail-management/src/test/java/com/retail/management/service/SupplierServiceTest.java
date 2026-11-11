
package com.retail.management.service;

import com.retail.management.dto.SupplierRequest;
import com.retail.management.dto.SupplierResponse;
import com.retail.management.entity.Supplier;
import com.retail.management.exception.DuplicateSupplierException;
import com.retail.management.exception.ResourceNotFoundException;
import com.retail.management.repository.SupplierRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SupplierServiceTest {

    @Mock
    private SupplierRepository supplierRepository;

    @InjectMocks
    private SupplierService supplierService;

    private SupplierRequest createRequest() {
        SupplierRequest request = new SupplierRequest();
        request.setSupplierName("ABC Technology");
        request.setContactPerson("John Smith");
        request.setEmail("abc@gmail.com");
        request.setPhoneNumber("0901234567");
        request.setAddress("Ho Chi Minh City");
        request.setActive(true);
        return request;
    }

    @Test
    void createSupplier_ShouldReturnSupplier_WhenValidRequest() {
        SupplierRequest request = createRequest();

        when(supplierRepository.existsByEmail(request.getEmail()))
                .thenReturn(false);

        when(supplierRepository.save(any(Supplier.class)))
                .thenAnswer(invocation -> {
                    Supplier supplier = invocation.getArgument(0);
                    supplier.setId(1L);
                    return supplier;
                });

        SupplierResponse response =
                supplierService.createSupplier(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("ABC Technology", response.getSupplierName());
        assertEquals("abc@gmail.com", response.getEmail());

        verify(supplierRepository).save(any(Supplier.class));
    }

    @Test
    void createSupplier_ShouldThrowException_WhenEmailExists() {
        SupplierRequest request = createRequest();

        when(supplierRepository.existsByEmail(request.getEmail()))
                .thenReturn(true);

        assertThrows(
                DuplicateSupplierException.class,
                () -> supplierService.createSupplier(request)
        );

        verify(supplierRepository, never()).save(any());
    }

    @Test
    void getAllSuppliers_ShouldReturnSupplierList() {
        Supplier supplier = new Supplier();
        supplier.setId(1L);
        supplier.setSupplierName("ABC Technology");
        supplier.setEmail("abc@gmail.com");

        when(supplierRepository.findAll())
                .thenReturn(List.of(supplier));

        List<SupplierResponse> result =
                supplierService.getAllSuppliers();

        assertEquals(1, result.size());
        assertEquals("ABC Technology",
                result.get(0).getSupplierName());
    }

    @Test
    void getSupplierById_ShouldReturnSupplier_WhenExists() {
        Supplier supplier = new Supplier();
        supplier.setId(1L);
        supplier.setSupplierName("ABC Technology");
        supplier.setEmail("abc@gmail.com");

        when(supplierRepository.findById(1L))
                .thenReturn(Optional.of(supplier));

        SupplierResponse response =
                supplierService.getSupplierById(1L);

        assertEquals(1L, response.getId());
        assertEquals("ABC Technology", response.getSupplierName());
    }

    @Test
    void getSupplierById_ShouldThrowException_WhenNotFound() {
        when(supplierRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> supplierService.getSupplierById(999L)
        );
    }

    @Test
    void updateSupplier_ShouldReturnUpdatedSupplier_WhenValidRequest() {
        Supplier supplier = new Supplier();
        supplier.setId(1L);
        supplier.setSupplierName("ABC Technology");
        supplier.setEmail("abc@gmail.com");

        SupplierRequest request = createRequest();
        request.setSupplierName("ABC Updated");

        when(supplierRepository.findById(1L))
                .thenReturn(Optional.of(supplier));

        when(supplierRepository.findByEmail(request.getEmail()))
                .thenReturn(Optional.of(supplier));

        when(supplierRepository.save(any(Supplier.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SupplierResponse response =
                supplierService.updateSupplier(1L, request);

        assertEquals("ABC Updated", response.getSupplierName());
        assertEquals("abc@gmail.com", response.getEmail());
    }

    @Test
    void updateSupplier_ShouldThrowException_WhenEmailBelongsToAnotherSupplier() {
        Supplier supplier = new Supplier();
        supplier.setId(1L);

        Supplier anotherSupplier = new Supplier();
        anotherSupplier.setId(2L);
        anotherSupplier.setEmail("abc@gmail.com");

        SupplierRequest request = createRequest();

        when(supplierRepository.findById(1L))
                .thenReturn(Optional.of(supplier));

        when(supplierRepository.findByEmail(request.getEmail()))
                .thenReturn(Optional.of(anotherSupplier));

        assertThrows(
                DuplicateSupplierException.class,
                () -> supplierService.updateSupplier(1L, request)
        );

        verify(supplierRepository, never()).save(any());
    }

    @Test
    void deleteSupplier_ShouldDelete_WhenExists() {
        Supplier supplier = new Supplier();
        supplier.setId(1L);

        when(supplierRepository.findById(1L))
                .thenReturn(Optional.of(supplier));

        supplierService.deleteSupplier(1L);

        verify(supplierRepository).delete(supplier);
    }

    @Test
    void deleteSupplier_ShouldThrowException_WhenNotFound() {
        when(supplierRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> supplierService.deleteSupplier(999L)
        );

        verify(supplierRepository, never()).delete(any());
    }
}
