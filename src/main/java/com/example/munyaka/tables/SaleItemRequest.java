package com.example.munyaka.tables;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class SaleItemRequest {
    private Long productId;
    private int quantity;
    private double total;
    private int price;
    private int discountAmount;
}