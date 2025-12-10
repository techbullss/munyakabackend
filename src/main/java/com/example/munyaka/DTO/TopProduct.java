package com.example.munyaka.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopProduct {
    private String productName;
    private Double totalSold;
    private Double totalRevenue;
}
