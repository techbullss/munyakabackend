package com.example.munyaka.repository;

import com.example.munyaka.tables.Purchase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> , JpaSpecificationExecutor<Purchase> {
    Page<Purchase> findBySupplierNameContainingIgnoreCase(String supplier, Pageable pageable);
    @Query("""
   SELECT p FROM Purchase p
   WHERE p.creditor = true
     AND p.balanceDue > 0
""")
    Page<Purchase> findCreditors(Pageable pageable);
}

