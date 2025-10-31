package com.example.munyaka.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DebtorDTO {
    private Long id;
    private String customerName;
    private String customerPhone;
    private Double totalDebt;
    private String lastSaleDate;
    private String paymentStatus;
    private List<SaleDTO> sales;
    private String lastPaymentDate;
    private Double lastPaymentAmount;
}