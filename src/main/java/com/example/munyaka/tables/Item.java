package com.example.munyaka.tables;



import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String itemName;
    private String category;
    @Column(length = 1000)
    private String description;
    private Double price;
    private Integer stockQuantity;
    private Double sellingPrice;
    private String supplier;
    @Enumerated(EnumType.STRING)
    private SellingUnit sellingUnit;
    @Enumerated(EnumType.STRING)
    private LengthType lengthType;
    private Integer piecesPerBox;
    @ElementCollection
    @CollectionTable(name = "item_images", joinColumns = @JoinColumn(name = "item_id"))
    @Column(name = "image_url")
    @Builder.Default
    private List<String> imageUrls = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "item_variants", joinColumns = @JoinColumn(name = "item_id"))
    @MapKeyColumn(name = "variant_name")
    @Column(name = "variant_value")
    @Builder.Default
    private Map<String, String> variants = new HashMap<>();

    // Helper methods
    public void addVariant(String name, String value) {
        this.variants.put(name, value);
    }

    public void addImageUrl(String imageUrl) {
        this.imageUrls.add(imageUrl);
    }
}
