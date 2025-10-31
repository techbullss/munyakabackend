package com.example.munyaka.tables;


import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemResponseDTO {
    private Long id;
    private String itemName;
    private String category;
    private String description;
    private Double price;
    private Integer stockQuantity;
    private Double sellingPrice;
    private String supplier;
    private SellingUnit sellingUnit;
    private LengthType lengthType;
    private Integer piecesPerBox;
    private List<String> imageUrls;
    private Map<String, String> variants;
}
