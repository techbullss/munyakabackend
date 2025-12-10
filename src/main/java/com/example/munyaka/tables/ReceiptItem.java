package com.example.munyaka.tables;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public  class ReceiptItem {
    private String productName;
    private double quantity;
    private double unitPrice;
    private double lineTotal;
    private String paymentMethod;
    private Double profit;

    // getters/setters
}
