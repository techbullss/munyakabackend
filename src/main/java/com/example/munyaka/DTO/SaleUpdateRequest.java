package com.example.munyaka.DTO;


import lombok.Data;

@Data
public class SaleUpdateRequest {
    private String customerName;
    private String customerPhone;
    private String paymentMethod;
    private String saleDate;
    private Double paidAmount;
}
