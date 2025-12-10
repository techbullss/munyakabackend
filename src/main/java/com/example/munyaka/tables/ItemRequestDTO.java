package com.example.munyaka.tables;

import com.example.munyaka.tables.LengthType;
import com.example.munyaka.tables.SellingUnit;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemRequestDTO {
    @NotBlank(message = "Item name is required")
    private String itemName;

    @NotBlank(message = "Category is required")
    private String category;

    private String description;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private Double price;

    @NotNull(message = "Stock quantity is required")
    @Positive(message = "Stock quantity must be positive")
    private Double stockQuantity;

    @NotNull(message = "Selling price is required")
    @Positive(message = "Selling price must be positive")
    private Double sellingPrice;

    private String supplier;

    @NotNull(message = "Selling unit is required")
    private SellingUnit sellingUnit;

    private LengthType lengthType;

    private Integer piecesPerBox;

    private List<String> images;

    private Map<String, String> variants;
}