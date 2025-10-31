package com.example.munyaka.services;



import com.example.munyaka.repository.RentalItemRepository;
import com.example.munyaka.tables.RentalItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class RentalItemService {

    @Autowired
    private RentalItemRepository rentalItemRepository;

    public List<RentalItem> getAllRentalItems() {
        return rentalItemRepository.findAll();
    }

    public Optional<RentalItem> getRentalItemById(Long id) {
        return rentalItemRepository.findById(id);
    }

    public List<RentalItem> getRentalItemsByCategory(String category) {
        return rentalItemRepository.findByCategory(category);
    }

    public List<RentalItem> searchRentalItems(String keyword) {
        return rentalItemRepository.searchByNameOrDescription(keyword);
    }

    public RentalItem createRentalItem(RentalItem rentalItem) {
        return rentalItemRepository.save(rentalItem);
    }

    public RentalItem updateRentalItem(Long id, RentalItem rentalItemDetails) {
        RentalItem rentalItem = rentalItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("RentalItem not found with id: " + id));

        rentalItem.setName(rentalItemDetails.getName());
        rentalItem.setDescription(rentalItemDetails.getDescription());
        rentalItem.setCategory(rentalItemDetails.getCategory());
        rentalItem.setDailyRate(rentalItemDetails.getDailyRate());
        rentalItem.setDepositAmount(rentalItemDetails.getDepositAmount());
        rentalItem.setAvailableQuantity(rentalItemDetails.getAvailableQuantity());
        rentalItem.setCondition(rentalItemDetails.getCondition());
        rentalItem.setImageUrl(rentalItemDetails.getImageUrl());
        rentalItem.setMaintenanceDate(rentalItemDetails.getMaintenanceDate());
        rentalItem.setIsActive(rentalItemDetails.getIsActive());

        return rentalItemRepository.save(rentalItem);
    }

    public void deleteRentalItem(Long id) {
        RentalItem rentalItem = rentalItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("RentalItem not found with id: " + id));

        rentalItemRepository.delete(rentalItem);
    }

    public List<RentalItem> getActiveRentalItems() {
        return rentalItemRepository.findByIsActiveTrue();
    }

    // CORRECTED: Get items needing maintenance
    public List<RentalItem> getItemsNeedingMaintenance() {
        LocalDate sixMonthsAgo = LocalDate.now().minusMonths(6);
        return rentalItemRepository.findItemsNeedingMaintenance(sixMonthsAgo);
    }
}