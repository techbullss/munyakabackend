package com.example.munyaka.repository;

import com.example.munyaka.tables.Expenditure;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface ExpenditureRepository extends JpaRepository<Expenditure, Long> {
    Page<Expenditure> findByCategoryContainingIgnoreCase(String category, Pageable pageable);
    Page<Expenditure> findByDescriptionContainingIgnoreCase(String search, Pageable pageable);
    Page<Expenditure> findByCategoryContainingIgnoreCaseAndDescriptionContainingIgnoreCase(String category, String search, Pageable pageable);

    // New methods for date filtering
    Page<Expenditure> findByDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);
    Page<Expenditure> findByDateBetweenAndCategoryContainingIgnoreCase(LocalDate startDate, LocalDate endDate, String category, Pageable pageable);
    Page<Expenditure> findByDateBetweenAndDescriptionContainingIgnoreCase(LocalDate startDate, LocalDate endDate, String search, Pageable pageable);
    Page<Expenditure> findByDateBetweenAndCategoryContainingIgnoreCaseAndDescriptionContainingIgnoreCase(
            LocalDate startDate, LocalDate endDate, String category, String search, Pageable pageable);
}

