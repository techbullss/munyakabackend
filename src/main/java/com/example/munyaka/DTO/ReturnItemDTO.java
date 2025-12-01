package com.example.munyaka.DTO;

import lombok.Data;

@Data
public class ReturnItemDTO {
    private Long productId;
    private int quantity; // returnQty
    private String reason;
    private String condition;
}
