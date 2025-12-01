package com.example.munyaka.DTO;


import java.math.BigDecimal;
import java.time.LocalDate;

public record CreditorResponse(
        Long id,
        String supplierName,
        String supplierPhone,
        String supplierEmail,

        BigDecimal balance,
        LocalDate dueDate,
        String status,
        String creditTerms,

        BigDecimal paymentAmount
) {}
