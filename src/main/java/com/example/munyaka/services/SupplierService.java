package com.example.munyaka.services;

import com.example.munyaka.repository.SupplierRepository;
import com.example.munyaka.tables.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository supplierRepository;

    public Page<Supplier> searchSuppliers(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        if (query == null || query.isBlank()) {
            return supplierRepository.findAll(pageable);
        }

        return supplierRepository
                .findByNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrPhoneContaining(
                        query, query, query, pageable
                );
    }

    public Supplier save(Supplier supplier) {
        return supplierRepository.save(supplier);
    }

    public Supplier update(Long id, Supplier supplier) {
        Supplier existing = supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found"));
        existing.setName(supplier.getName());
        existing.setEmail(supplier.getEmail());
        existing.setPhone(supplier.getPhone());
        existing.setAddress(supplier.getAddress());
        return supplierRepository.save(existing);
    }

    public void delete(Long id) {
        supplierRepository.deleteById(id);
    }

    public Supplier findById(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found"));
    }
}

