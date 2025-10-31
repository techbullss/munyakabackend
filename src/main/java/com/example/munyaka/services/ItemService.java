package com.example.munyaka.services;
import com.example.munyaka.tables.ItemRequestDTO;
import com.example.munyaka.tables.ItemResponseDTO;

import java.util.List;

public interface ItemService {
    void updateItemStock(Long id, Integer stockQuantity);
    ItemResponseDTO createItem(ItemRequestDTO itemRequestDTO);
    ItemResponseDTO getItemById(Long id);
    List<ItemResponseDTO> getAllItems();
    List<ItemResponseDTO> getItemsByCategory(String category);
    ItemResponseDTO updateItem(Long id, ItemRequestDTO itemRequestDTO);
    void deleteItem(Long id);
    List<ItemResponseDTO> searchItems(String keyword);
    String[] getAvailableVariantsForCategory(String category);
}
