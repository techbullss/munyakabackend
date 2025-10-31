package com.example.munyaka.DTO;

import com.example.munyaka.tables.SaleItemDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleDTO {
    private Long id;
    private String customerPhone;
    private String customerName;
    private String saleDate;
    private Double totalAmount;
    private Double paidAmount;
    private Double changeAmount;
    private Double balanceDue;
    private String paymentMethod;
    private Double profit;
    private String paymentStatus;
    private List<SaleItemDTOs> items;
}
