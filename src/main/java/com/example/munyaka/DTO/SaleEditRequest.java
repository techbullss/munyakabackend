package com.example.munyaka.DTO;

import com.example.munyaka.tables.SaleItemRequest;
import lombok.Data;
import java.util.List;

@Data
public class SaleEditRequest {
    private String customerName;
    private String customerPhone;
    private String paymentMethod;
    private String saleDate; // Optional for editing date
    private Double paidAmount;
    private List<SaleItemRequest> items; // Full list of items
}
