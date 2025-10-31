package com.example.munyaka.repository;


import com.example.munyaka.tables.RentalItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RentalItemRepository extends JpaRepository<RentalItem, Long> {

    // Find by category
    List<RentalItem> findByCategory(String category);

    // Find active rental items
    List<RentalItem> findByIsActiveTrue();

    // Search by name or description
    @Query("SELECT r FROM RentalItem r WHERE LOWER(r.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(r.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<RentalItem> searchByNameOrDescription(@Param("keyword") String keyword);

    // CORRECTED: Find items that need maintenance (older than 6 months)
    @Query("SELECT r FROM RentalItem r WHERE r.maintenanceDate < :sixMonthsAgo")
    List<RentalItem> findItemsNeedingMaintenance(@Param("sixMonthsAgo") LocalDate sixMonthsAgo);
}