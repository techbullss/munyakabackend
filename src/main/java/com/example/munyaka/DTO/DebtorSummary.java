package com.example.munyaka.DTO;

import com.example.munyaka.tables.Sale;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DebtorSummary {
    private String customerName;
    private String customerPhone;
    private Double totalDebt;
    private String lastSaleDate;
    private String paymentStatus;
    private List<Sale> sales;
}