package com.example.munyaka.controllers;
import com.example.munyaka.services.ItemService;
import com.example.munyaka.tables.ItemRequestDTO;
import com.example.munyaka.tables.ItemResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/items")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    public ResponseEntity<ItemResponseDTO> createItem(@Valid @RequestBody ItemRequestDTO itemRequestDTO) {
        ItemResponseDTO createdItem = itemService.createItem(itemRequestDTO);
        return new ResponseEntity<>(createdItem, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemResponseDTO> getItemById(@PathVariable Long id) {
        ItemResponseDTO item = itemService.getItemById(id);
        return ResponseEntity.ok(item);
    }

    @GetMapping
    public ResponseEntity<List<ItemResponseDTO>> getAllItems() {
        List<ItemResponseDTO> items = itemService.getAllItems();
        return ResponseEntity.ok(items);
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<ItemResponseDTO>> getItemsByCategory(@PathVariable String category) {
        List<ItemResponseDTO> items = itemService.getItemsByCategory(category);
        return ResponseEntity.ok(items);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ItemResponseDTO> updateItem(
            @PathVariable Long id,
            @Valid @RequestBody ItemRequestDTO itemRequestDTO) {
        ItemResponseDTO updatedItem = itemService.updateItem(id, itemRequestDTO);
        return ResponseEntity.ok(updatedItem);
    }

    // New endpoint for updating stock only
    @PatchMapping("/{id}/stock")
    public ResponseEntity<Void> updateItemStock(
            @PathVariable Long id,
            @RequestParam Integer stockQuantity) {
        itemService.updateItemStock(id, stockQuantity);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        itemService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<ItemResponseDTO>> searchItems(@RequestParam String keyword) {
        List<ItemResponseDTO> items = itemService.searchItems(keyword);
        return ResponseEntity.ok(items);
    }

    @GetMapping("/variants/{category}")
    public ResponseEntity<String[]> getAvailableVariants(@PathVariable String category) {
        String[] variants = itemService.getAvailableVariantsForCategory(category);
        return ResponseEntity.ok(variants);
    }
}