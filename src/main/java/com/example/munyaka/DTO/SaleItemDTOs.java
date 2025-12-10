package com.example.munyaka.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleItemDTOs {
    private Long id;
    private String productName;
    private double quantity;
    private Double unitPrice;
    private Double totalPrice;
}
