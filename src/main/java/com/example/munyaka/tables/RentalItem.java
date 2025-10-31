package com.example.munyaka.tables;





import jakarta.persistence.Entity;
import jakarta.persistence.*;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "rental_items")
public class RentalItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String category;

    @Column(name = "daily_rate", nullable = false)
    private Double dailyRate;

    @Column(name = "deposit_amount", nullable = false)
    private Double depositAmount;

    @Column(name = "available_quantity", nullable = false)
    private Integer availableQuantity;

    @Column(name = "`condition`", nullable = false) // Escaped with backticks
    private String condition;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "maintenance_date")
    private LocalDate maintenanceDate;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    // Constructors
    public RentalItem() {}

    public RentalItem(String name, String description, String category, Double dailyRate,
                      Double depositAmount, Integer availableQuantity, String condition,
                      Boolean isActive) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.dailyRate = dailyRate;
        this.depositAmount = depositAmount;
        this.availableQuantity = availableQuantity;
        this.condition = condition;
        this.isActive = isActive;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Double getDailyRate() { return dailyRate; }
    public void setDailyRate(Double dailyRate) { this.dailyRate = dailyRate; }

    public Double getDepositAmount() { return depositAmount; }
    public void setDepositAmount(Double depositAmount) { this.depositAmount = depositAmount; }

    public Integer getAvailableQuantity() { return availableQuantity; }
    public void setAvailableQuantity(Integer availableQuantity) { this.availableQuantity = availableQuantity; }

    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public LocalDate getMaintenanceDate() { return maintenanceDate; }
    public void setMaintenanceDate(LocalDate maintenanceDate) { this.maintenanceDate = maintenanceDate; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}
