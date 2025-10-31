package com.example.munyaka.repository;


import com.example.munyaka.tables.RentalTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RentalTransactionRepository extends JpaRepository<RentalTransaction, Long> {

    List<RentalTransaction> findByStatus(String status);
    @Query("SELECT r FROM RentalTransaction r WHERE r.status = 'COMPLETED_PENDING_PAYMENT'")
    List<RentalTransaction> findCompletedPendingPaymentRentals();
    List<RentalTransaction> findByCustomerPhone(String customerPhone);

    List<RentalTransaction> findByRentalItemId(Long rentalItemId);

    @Query("SELECT r FROM RentalTransaction r WHERE r.expectedReturnDate < :today AND r.status = 'ACTIVE'")
    List<RentalTransaction> findOverdueRentals(@Param("today") LocalDate today);

    @Query("SELECT r FROM RentalTransaction r WHERE r.rentalStartDate BETWEEN :startDate AND :endDate")
    List<RentalTransaction> findRentalsBetweenDates(@Param("startDate") LocalDate startDate,
                                                    @Param("endDate") LocalDate endDate);
}