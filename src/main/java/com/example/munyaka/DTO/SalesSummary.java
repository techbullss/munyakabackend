package com.example.munyaka.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SalesSummary {
    private Double totalSales;
    private Double todaySales;
    private Double monthlySales;
    private Double salesChange;
    private List<MonthlySalesData> monthlyBreakdown; // this is the extra one
}
