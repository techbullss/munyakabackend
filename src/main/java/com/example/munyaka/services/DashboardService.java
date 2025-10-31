package com.example.munyaka.services;

import com.example.munyaka.DTO.*;
import com.example.munyaka.repository.ItemRepository;
import com.example.munyaka.repository.SaleRepository;
import com.example.munyaka.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class DashboardService {

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository productRepository;

    public DashboardDTO getDashboardData() {
        try {
            Map<String, Object> quickStats = getQuickStats();
            SalesSummary salesSummary = getSalesSummary();
            List<RecentActivity> recentActivities = getRecentActivities();
            List<TopProduct> topProducts = getTopProducts();

            return new DashboardDTO(salesSummary, recentActivities, topProducts, quickStats);

        } catch (Exception e) {
            throw new RuntimeException("Error fetching dashboard data: " + e.getMessage());
        }
    }

    private Map<String, Object> getQuickStats() {
        Map<String, Object> stats = new HashMap<>();

        long totalUsers = userRepository.count();
        long totalSales = saleRepository.count();
        double totalRevenue = Optional.ofNullable(saleRepository.getTotalSalesAmount()).orElse(0.0);
        double totalDebt = Optional.ofNullable(saleRepository.getTotalDebtAmount()).orElse(0.0);

        stats.put("totalUsers", totalUsers);
        stats.put("totalSales", totalSales);
        stats.put("totalRevenue", totalRevenue);
        stats.put("totalDebt", totalDebt);
        stats.put("profitMargin", calculateProfitMargin(totalRevenue, totalDebt));

        return stats;
    }

    private double calculateProfitMargin(double revenue, double debt) {
        if (revenue == 0) return 0;
        return ((revenue - debt) / revenue) * 100;
    }

    private SalesSummary getSalesSummary() {
        Double totalSales = Optional.ofNullable(saleRepository.getTotalSalesAmount()).orElse(0.0);

        // Include today’s range correctly
        LocalDate today = LocalDate.now();
        Double todaySales = Optional.ofNullable(
                saleRepository.getSalesAmountByDateRange(today, today)
        ).orElse(0.0);

        // Current month range
        LocalDate startOfMonth = today.withDayOfMonth(1);
        Double monthlySales = Optional.ofNullable(
                saleRepository.getSalesAmountByDateRange(startOfMonth, today)
        ).orElse(0.0);

        // Example: You can later replace this with a dynamic % change calc
        Double salesChange = monthlySales > 0 ? (todaySales / monthlySales) * 100 : 0.0;

        List<MonthlySalesData> breakdown = getMonthlyBreakdown(); // optional
        return new SalesSummary(totalSales, todaySales, monthlySales, salesChange, breakdown);
    }

    private List<MonthlySalesData> getMonthlyBreakdown() {
        List<MonthlySalesData> breakdown = new ArrayList<>();

        LocalDate today = LocalDate.now();
        LocalDate startOfMonth = today.withDayOfMonth(1);

        // Loop from start of the month until today
        for (LocalDate date = startOfMonth; !date.isAfter(today); date = date.plusDays(1)) {
            Double dailySales = Optional.ofNullable(
                    saleRepository.getSalesAmountByDateRange(date, date)
            ).orElse(0.0);

            breakdown.add(new MonthlySalesData(
                    date.toString(),
                    dailySales
            ));
        }

        return breakdown;
    }

    private List<RecentActivity> getRecentActivities() {
        List<RecentActivity> activities = new ArrayList<>();
        var recentSales = saleRepository.findTop10ByOrderBySaleDateDesc();

        for (var sale : recentSales) {
            activities.add(new RecentActivity(
                    "sale",
                    "New sale completed",
                    "Sale #" + sale.getId(),
                    "KSh " + sale.getTotalAmount(),
                    formatTimeAgo(sale.getSaleDate())
            ));
        }

        return activities;
    }

    private List<TopProduct> getTopProducts() {
        List<TopProduct> topProducts = new ArrayList<>();
        var productSales = saleRepository.findTopSellingProducts();
        for (var productSale : productSales) {
            topProducts.add(new TopProduct(
                    productSale.getProductName(),
                    productSale.getTotalSold(),
                    productSale.getTotalRevenue()
            ));
        }
        return topProducts;
    }

    private String formatTimeAgo(LocalDate saleDate) {
        if (saleDate == null) return "Unknown time";

        LocalDate today = LocalDate.now();
        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(saleDate, today);

        if (daysBetween == 0) return "Today";
        else if (daysBetween == 1) return "Yesterday";
        else return daysBetween + " days ago";
    }
}
