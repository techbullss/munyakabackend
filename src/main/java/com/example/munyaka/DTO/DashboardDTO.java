package com.example.munyaka.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDTO {
    private SalesSummary salesSummary;
    private List<RecentActivity> recentActivities;
    private List<TopProduct> topProducts;
    private Map<String, Object> quickStats;
}

