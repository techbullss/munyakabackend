package com.example.munyaka.tables;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
@Data
@Setter
@Getter
public class SaleSummaryDTO {
    private Long id;
    private String customerName;
    private String saleDate;
    private double totalAmount;
    private String paymentMethod;
    private double profit;
    private double balance;
    private double paidAmount;
    private String paymentStatus;
    private String customerPhone;
    private Boolean isDeleted;
    private LocalDate deletedAt;
    private List<SaleItemDTO> items;

    public SaleSummaryDTO(Sale sale) {
        this.id = sale.getId();
        this.customerName = sale.getCustomerName();
        this.saleDate = String.valueOf(sale.getSaleDate());
        this.paymentMethod=sale.getPaymentMethod();
        this.profit = sale.getProfit();
        this.paidAmount= sale.getPaidAmount();
        this.balance=sale.getBalanceDue();
        this.paymentStatus = sale.getPaymentStatus();
        this.totalAmount = sale.getTotalAmount() != null ? sale.getTotalAmount() : 0.0;
        this.items = sale.getItems().stream().map(SaleItemDTO::new).toList();
        this.customerPhone=sale.getCustomerPhone();
        this.isDeleted = sale.getIsDeleted();
        this.deletedAt = sale.getDeletedAt();
    }

    // getters
}
