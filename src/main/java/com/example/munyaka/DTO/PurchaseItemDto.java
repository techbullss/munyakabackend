package com.example.munyaka.DTO;

import java.math.BigDecimal;

public record PurchaseItemDto(
        Long id,
        String productName,
        BigDecimal price,
        int quantity,
        BigDecimal total
) {}
