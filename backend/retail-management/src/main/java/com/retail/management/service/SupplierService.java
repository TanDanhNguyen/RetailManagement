
package com.retail.management.service;

import com.retail.management.dto.SupplierRequest;
import com.retail.management.dto.SupplierResponse;
import com.retail.management.entity.Supplier;
import com.retail.management.exception.DuplicateSupplierException;
import com.retail.management.exception.ResourceNotFoundException;
import com.retail.management.repository.SupplierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SupplierService {

    private final SupplierRepository supplierRepository;

    public SupplierService(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    @Transactional
    public SupplierResponse createSupplier(SupplierRequest request) {

        if (supplierRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateSupplierException(
                    "Supplier email already exists: " + request.getEmail()
            );
        }

        Supplier supplier = new Supplier();
        mapToEntity(request, supplier);

        Supplier savedSupplier = supplierRepository.save(supplier);

        return mapToResponse(savedSupplier);
    }

    @Transactional(readOnly = true)
    public List<SupplierResponse> getAllSuppliers() {

        return supplierRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SupplierResponse getSupplierById(Long id) {

        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Supplier not found: " + id
                        )
                );

        return mapToResponse(supplier);
    }

    @Transactional
    public SupplierResponse updateSupplier(
            Long id,
            SupplierRequest request) {

        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Supplier not found: " + id
                        )
                );

        boolean emailExists = supplierRepository
                .findByEmail(request.getEmail())
                .filter(existing ->
                        !existing.getId().equals(id))
                .isPresent();

        if (emailExists) {
            throw new DuplicateSupplierException(
                    "Supplier email already exists: " + request.getEmail()
            );
        }

        mapToEntity(request, supplier);

        Supplier updatedSupplier = supplierRepository.save(supplier);

        return mapToResponse(updatedSupplier);
    }

    @Transactional
    public void deleteSupplier(Long id) {

        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Supplier not found: " + id
                        )
                );

        supplierRepository.delete(supplier);
    }

    private void mapToEntity(
            SupplierRequest request,
            Supplier supplier) {

        supplier.setSupplierName(request.getSupplierName());
        supplier.setContactPerson(request.getContactPerson());
        supplier.setEmail(request.getEmail());
        supplier.setPhoneNumber(request.getPhoneNumber());
        supplier.setAddress(request.getAddress());
        supplier.setActive(request.getActive());
    }

    private SupplierResponse mapToResponse(Supplier supplier) {

        return new SupplierResponse(
                supplier.getId(),
                supplier.getSupplierName(),
                supplier.getContactPerson(),
                supplier.getEmail(),
                supplier.getPhoneNumber(),
                supplier.getAddress(),
                supplier.getActive(),
                supplier.getCreatedAt()
        );
    }
}
