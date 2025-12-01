package com.example.munyaka.services;

import com.example.munyaka.DTO.CreditorResponse;
import com.example.munyaka.repository.PurchaseRepository;
import com.example.munyaka.tables.Purchase;
import org.springframework.transaction.annotation.Transactional;import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CreditorService {
    private final PurchaseRepository purchaseRepo;
    @Transactional
    public CreditorResponse recordPayment(Long id, double amount) {
        Purchase purchase = purchaseRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Creditor not found"));

        if (amount <= 0) {
            throw new IllegalArgumentException("Payment amount must be positive");
        }

        BigDecimal payment = BigDecimal.valueOf(amount);

        // Compare using compareTo
        if (payment.compareTo(purchase.getBalanceDue()) > 0) {
            throw new IllegalArgumentException("Payment exceeds current balance");
        }

        // Update using BigDecimal arithmetic
        BigDecimal newBalance = purchase.getBalanceDue().subtract(payment);
        BigDecimal newPaid    = purchase.getAmountPaid().add(payment);

        purchase.setBalanceDue(newBalance);
        purchase.setAmountPaid(newPaid);

        if (newBalance.compareTo(BigDecimal.ZERO) <= 0) {
            purchase.setStatus("Paid");
        } else {
            purchase.setStatus("Pending");
        }

        Purchase saved = purchaseRepo.save(purchase);
        return toDto(saved);
    }


    @Transactional(readOnly = true)
    public Page<CreditorResponse> listCreditors(Pageable pageable) {
        return purchaseRepo.findCreditors(pageable).map(this::toDto);
    }

    private CreditorResponse toDto(Purchase p) {
        return new CreditorResponse(
                p.getId(),
                p.getSupplierName(),
                p.getSupplierEmail(),
                p.getSupplierPhone(),


                p.getBalanceDue(),
                p.getPurchaseDate().plusDays(30), // example due date
                p.getStatus(),
                "Net 30",
                    // if you track payments
                p.getAmountPaid()
        );
    }
}
