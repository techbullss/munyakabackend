package com.example.munyaka.controllers;
import com.example.munyaka.services.RentalItemService;
import com.example.munyaka.tables.RentalItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/rental-items")
@CrossOrigin(origins = "http://localhost:3000")
public class RentalItemController {

    @Autowired
    private RentalItemService rentalItemService;

    @GetMapping
    public List<RentalItem> getAllRentalItems() {
        return rentalItemService.getAllRentalItems();
    }

    @GetMapping("/{id}")
    public ResponseEntity<RentalItem> getRentalItemById(@PathVariable Long id) {
        Optional<RentalItem> rentalItem = rentalItemService.getRentalItemById(id);
        return rentalItem.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/category/{category}")
    public List<RentalItem> getRentalItemsByCategory(@PathVariable String category) {
        return rentalItemService.getRentalItemsByCategory(category);
    }

    @GetMapping("/search")
    public List<RentalItem> searchRentalItems(@RequestParam String keyword) {
        return rentalItemService.searchRentalItems(keyword);
    }

    @GetMapping("/active")
    public List<RentalItem> getActiveRentalItems() {
        return rentalItemService.getActiveRentalItems();
    }

    @GetMapping("/maintenance-needed")
    public List<RentalItem> getItemsNeedingMaintenance() {
        return rentalItemService.getItemsNeedingMaintenance();
    }

    @PostMapping
    public RentalItem createRentalItem(@RequestBody RentalItem rentalItem) {
        return rentalItemService.createRentalItem(rentalItem);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RentalItem> updateRentalItem(@PathVariable Long id, @RequestBody RentalItem rentalItemDetails) {
        try {
            RentalItem updatedRentalItem = rentalItemService.updateRentalItem(id, rentalItemDetails);
            return ResponseEntity.ok(updatedRentalItem);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRentalItem(@PathVariable Long id) {
        try {
            rentalItemService.deleteRentalItem(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}