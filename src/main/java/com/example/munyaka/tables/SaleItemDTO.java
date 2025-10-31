package com.example.munyaka.tables;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class SaleItemDTO {
    private Long productId;
    private String productName;
    private int quantity;
    private double unitPrice;
    private double lineTotal;
    private double discount;
    private double profit;
 private double buyingPrice;
    public SaleItemDTO(SaleItem item) {
        this.productName = item.getProduct().getItemName();
        this.quantity = item.getQuantity();
        this.productId=item.getProduct().getId();

        this.unitPrice = item.getProduct().getSellingPrice();
        this.buyingPrice=item.getProduct().getPrice();
        this.lineTotal = unitPrice * quantity;
        this.discount=item.getDiscountAmount();
        this.profit= item.getProfit();
    }

    // getters
}