package com.example.munyaka.repository;
import com.example.munyaka.tables.RentalPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RentalPaymentRepository extends JpaRepository<RentalPayment, Long> {

    List<RentalPayment> findByRentalTransactionId(Long rentalTransactionId);

    List<RentalPayment> findByRentalTransactionIdOrderByPaymentDateDesc(Long rentalTransactionId);
}