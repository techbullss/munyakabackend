package com.example.munyaka.services;

import com.example.munyaka.repository.ItemRepository;

import com.example.munyaka.tables.Item;
import com.example.munyaka.tables.ItemRequestDTO;
import com.example.munyaka.tables.ItemResponseDTO;
import com.example.munyaka.tables.SellingUnit;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;

    // Define category-specific variant templates using traditional HashMap approach
    private final Map<String, String[]> variantTemplates = createVariantTemplates();

    private Map<String, String[]> createVariantTemplates() {
        Map<String, String[]> templates = new HashMap<>();
        templates.put("Welding Materials", new String[]{"Size", "Gauge", "Type", "Color", "Brand"});
        templates.put("Building Materials", new String[]{"Size", "Color", "Material", "Brand", "Grade"});
        templates.put("Tools & Equipment", new String[]{"Type", "Size", "Power Source", "Brand", "Weight"});
        templates.put("Plumbing Supplies", new String[]{"Diameter", "Material", "Type", "Length", "Connection Type"});
        templates.put("Electrical Supplies", new String[]{"Voltage", "Current Rating", "Type", "Color", "Certification"});
        templates.put("Paints & Coatings", new String[]{"Color", "Finish", "Base", "Volume", "Drying Time"});
        templates.put("Hardware & Fasteners", new String[]{"Size", "Material", "Type", "Length", "Head Type"});
        templates.put("Safety Equipment", new String[]{"Size", "Material", "Type", "Certification", "Color"});
        templates.put("Garden & Outdoor", new String[]{"Size", "Material", "Type", "Color", "Weather Resistance"});
        templates.put("Chemicals & Adhesives", new String[]{"Type", "Volume", "Curing Time", "Color", "Application"});
        templates.put("Hardware Accessories", new String[]{"Size", "Material", "Type", "Color", "Brand"});
        return templates;
    }

    @Override
    public ItemResponseDTO createItem(ItemRequestDTO itemRequestDTO) {
        validateSellingUnitConstraints(itemRequestDTO);
        validateVariants(itemRequestDTO);

        Item item = convertToEntity(itemRequestDTO);
        Item savedItem = itemRepository.save(item);
        return convertToDTO(savedItem);
    }
    @Override
    @Transactional
    public void updateItemStock(Long id, Integer stockQuantity) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Item not found with id: " + id));

        item.setStockQuantity(stockQuantity);
        itemRepository.save(item);
    }
    @Override
    public ItemResponseDTO getItemById(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Item not found with id: " + id));
        return convertToDTO(item);
    }

    @Override
    public List<ItemResponseDTO> getAllItems() {
        return itemRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemResponseDTO> getItemsByCategory(String category) {
        return itemRepository.findByCategory(category).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ItemResponseDTO updateItem(Long id, ItemRequestDTO itemRequestDTO) {
        validateSellingUnitConstraints(itemRequestDTO);
        validateVariants(itemRequestDTO);

        Item existingItem = itemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Item not found with id: " + id));

        // Update fields
        existingItem.setItemName(itemRequestDTO.getItemName());
        existingItem.setCategory(itemRequestDTO.getCategory());
        existingItem.setDescription(itemRequestDTO.getDescription());
        existingItem.setPrice(itemRequestDTO.getPrice());
        existingItem.setStockQuantity(itemRequestDTO.getStockQuantity());
        existingItem.setSellingPrice(itemRequestDTO.getSellingPrice());
        existingItem.setSupplier(itemRequestDTO.getSupplier());
        existingItem.setSellingUnit(itemRequestDTO.getSellingUnit());
        existingItem.setLengthType(itemRequestDTO.getLengthType());
        existingItem.setPiecesPerBox(itemRequestDTO.getPiecesPerBox());

        // Update variants
        existingItem.getVariants().clear();
        if (itemRequestDTO.getVariants() != null) {
            existingItem.getVariants().putAll(itemRequestDTO.getVariants());
        }

        // Update images
        existingItem.getImageUrls().clear();
        if (itemRequestDTO.getImages() != null) {
            existingItem.getImageUrls().addAll(itemRequestDTO.getImages());
        }

        Item updatedItem = itemRepository.save(existingItem);
        return convertToDTO(updatedItem);
    }

    @Override
    public void deleteItem(Long id) {
        if (!itemRepository.existsById(id)) {
            throw new EntityNotFoundException("Item not found with id: " + id);
        }
        itemRepository.deleteById(id);
    }

    @Override
    public List<ItemResponseDTO> searchItems(String keyword) {
        return itemRepository.findByItemNameContainingIgnoreCase(keyword).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public String[] getAvailableVariantsForCategory(String category) {
        return variantTemplates.getOrDefault(category, new String[0]);
    }

    private void validateSellingUnitConstraints(ItemRequestDTO itemRequestDTO) {
        // Validate that piecesPerBox is provided when sellingUnit is BOXES
        if (itemRequestDTO.getSellingUnit() == SellingUnit.BOXES &&
                (itemRequestDTO.getPiecesPerBox() == null || itemRequestDTO.getPiecesPerBox() <= 0)) {
            throw new IllegalArgumentException("Pieces per box must be provided and greater than 0 for items sold in boxes");
        }

        // Validate that lengthType is provided when sellingUnit is LENGTH
        if (itemRequestDTO.getSellingUnit() == SellingUnit.LENGTH &&
                itemRequestDTO.getLengthType() == null) {
            throw new IllegalArgumentException("Length type must be provided for length-based items");
        }

        // Validate that piecesPerBox is not provided when sellingUnit is not BOXES
        if (itemRequestDTO.getSellingUnit() != SellingUnit.BOXES &&
                itemRequestDTO.getPiecesPerBox() != null) {
            throw new IllegalArgumentException("Pieces per box should only be provided for items sold in boxes");
        }

        // Validate that lengthType is not provided when sellingUnit is not LENGTH
        if (itemRequestDTO.getSellingUnit() != SellingUnit.LENGTH &&
                itemRequestDTO.getLengthType() != null) {
            throw new IllegalArgumentException("Length type should only be provided for length-based items");
        }
    }

    private void validateVariants(ItemRequestDTO itemRequestDTO) {
        String category = itemRequestDTO.getCategory();
        Map<String, String> variants = itemRequestDTO.getVariants();

        if (variants == null || variants.isEmpty()) {
            return; // No variants to validate
        }

        // Check if category has a variant template
        if (variantTemplates.containsKey(category)) {
            String[] expectedVariants = variantTemplates.get(category);

            // Validate that all provided variants are expected for this category
            for (String variantName : variants.keySet()) {
                boolean isValid = false;
                for (String expectedVariant : expectedVariants) {
                    if (expectedVariant.equalsIgnoreCase(variantName)) {
                        isValid = true;
                        break;
                    }
                }

                if (!isValid) {
                    throw new IllegalArgumentException(
                            "Variant '" + variantName + "' is not valid for category '" + category + "'. " +
                                    "Expected variants: " + Arrays.toString(expectedVariants)
                    );
                }
            }
        }
    }

    private Item convertToEntity(ItemRequestDTO dto) {
        Item item = Item.builder()
                .itemName(dto.getItemName())
                .category(dto.getCategory())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .stockQuantity(dto.getStockQuantity())
                .sellingPrice(dto.getSellingPrice())
                .supplier(dto.getSupplier())
                .sellingUnit(dto.getSellingUnit())
                .lengthType(dto.getLengthType())
                .piecesPerBox(dto.getPiecesPerBox())
                .build();

        // Add variants
        if (dto.getVariants() != null) {
            item.getVariants().putAll(dto.getVariants());
        }

        // Add images
        if (dto.getImages() != null) {
            item.getImageUrls().addAll(dto.getImages());
        }

        return item;
    }

    private ItemResponseDTO convertToDTO(Item item) {
        return ItemResponseDTO.builder()
                .id(item.getId())
                .itemName(item.getItemName())
                .category(item.getCategory())
                .description(item.getDescription())
                .price(item.getPrice())
                .stockQuantity(item.getStockQuantity())
                .sellingPrice(item.getSellingPrice())
                .supplier(item.getSupplier())
                .sellingUnit(item.getSellingUnit())
                .lengthType(item.getLengthType())
                .piecesPerBox(item.getPiecesPerBox())
                .imageUrls(item.getImageUrls())
                .variants(item.getVariants())
                .build();
    }
}