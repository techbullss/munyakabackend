package com.example.munyaka.tables;

import lombok.Data;

@Data
public class SaleItemRequest {
    private Long productId;
    private double quantity;
    private double total;
    private int price;
    private Integer discountAmount; // Changed from int to Integer
}