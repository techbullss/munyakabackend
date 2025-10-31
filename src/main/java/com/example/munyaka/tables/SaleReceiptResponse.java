package com.example.munyaka.tables;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Data
@Setter
@Getter
public class SaleReceiptResponse {
    private String customerName;
    private String customerPhone;
    private String saleDate;
    private List<ReceiptItem> items;
    private double totalAmount;
    private double paidAmount;
    private double change;
    private String paymentMethod;
    private Double profit;
}