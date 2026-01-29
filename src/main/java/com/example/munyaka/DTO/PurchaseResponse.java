package com.example.munyaka.DTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record PurchaseResponse(
        Long id,
        String supplierName,
        String supplierEmail,
        String supplierPhone,
        LocalDate purchaseDate,
        BigDecimal totalAmount,
        BigDecimal amountPaid,
        BigDecimal balanceDue,
        String status,
        boolean creditor,
        List<PurchaseItemDto> items
) {

}