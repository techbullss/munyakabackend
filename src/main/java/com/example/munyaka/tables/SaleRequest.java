package com.example.munyaka.tables;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
@Getter
@Setter
public class SaleRequest {
    private String customerName;
    private String customerPhone;
    private String paymentMethod;
    private double totalAmount;
    private double amountPaid;
    private double changeAmount;
    private List<SaleItemRequest> items;
}


