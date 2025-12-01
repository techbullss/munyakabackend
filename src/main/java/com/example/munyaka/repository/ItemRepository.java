package com.example.munyaka.repository;

import com.example.munyaka.DTO.TopProduct;
import com.example.munyaka.tables.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByCategory(String category);

    List<Item> findByItemNameContainingIgnoreCase(String itemName);

    List<Item> findBySupplier(String supplier);

    long countByStockQuantityLessThan(int quantity);

    // Find top selling products (you'll need to implement this based on your sales structure)

}
