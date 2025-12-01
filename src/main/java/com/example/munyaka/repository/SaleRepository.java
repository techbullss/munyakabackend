package com.example.munyaka.repository;

import com.example.munyaka.DTO.TopProduct;
import com.example.munyaka.tables.Sale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SaleRepository extends JpaRepository<Sale, Long> {
    @Query("SELECT s FROM Sale s WHERE s.saleDate BETWEEN :start AND :end")
    List<Sale> findBySaleDateBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT s FROM Sale s WHERE s.saleDate BETWEEN :start AND :end")
    Page<Sale> findBySaleDateBetween(@Param("start") LocalDate start, @Param("end") LocalDate end, Pageable pageable);

    List<Sale> findByPaymentStatusAndBalanceDueGreaterThan(String paymentStatus, Double minBalance);

    // Find sales by customer phone with pending payments
    List<Sale> findByCustomerPhoneAndPaymentStatusAndBalanceDueGreaterThan(
            String customerPhone, String paymentStatus, Double minBalance);

    List<Sale> findByBalanceDueGreaterThan(Double balanceDue);

    // Find sales by customer phone with balance due
    List<Sale> findByCustomerPhoneAndBalanceDueGreaterThan(String customerPhone, Double balanceDue);

    // Find distinct customers with pending payments
    @Query("SELECT DISTINCT s.customerPhone FROM Sale s WHERE s.balanceDue <0")
    List<String> findCustomersWithPendingPayments();
    List<Sale> findByPaymentStatus(String paymentStatus);


    // Get total sales amount
    @Query("SELECT SUM(s.totalAmount) FROM Sale s")
    Double getTotalSalesAmount();

    // Get sales amount by date range
    @Query("SELECT SUM(s.totalAmount) FROM Sale s WHERE s.saleDate BETWEEN :start AND :end")
    Double getSalesAmountByDateRange(@Param("start") LocalDate start, @Param("end") LocalDate end);

    // Find recent sales
    List<Sale> findTop10ByOrderBySaleDateDesc();
    @Query("SELECT SUM(s.balanceDue) FROM Sale s WHERE s.paymentStatus = 'Pending'")
    Double getTotalDebtAmount();
    @Query("""
    SELECT new com.example.munyaka.DTO.TopProduct(
        si.product.itemName,
        SUM(si.quantity),
        SUM(si.total)
    )
    FROM SaleItem si
    GROUP BY si.product.itemName
    ORDER BY SUM(si.quantity) DESC
""")
    List<TopProduct> findTopSellingProducts();
    @Query("SELECT DISTINCT s FROM Sale s LEFT JOIN FETCH s.items i LEFT JOIN FETCH i.product WHERE s.id = :id")
    Optional<Sale> findByIdWithItems(@Param("id") Long id);
}
