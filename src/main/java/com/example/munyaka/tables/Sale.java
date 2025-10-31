package com.example.munyaka.tables;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sale {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private  String customerPhone;
    private String customerName;
    private LocalDate saleDate;
    private Double totalAmount;
    private Double paidAmount;
    private Double changeAmount;
    private Double balanceDue;
    private String paymentMethod;
    private  double profit;
    private String paymentStatus;
    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)

    private List<SaleItem> items = new ArrayList<>();
}
