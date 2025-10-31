package com.example.munyaka.tables;



import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@Setter
@Getter
@Entity
@Table(name = "rental_transactions")
public class RentalTransaction {
    // Getters and setters

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Getter
    @ManyToOne
    @JoinColumn(name = "rental_item_id", nullable = false)
    private RentalItem rentalItem;

    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Column(name = "customer_phone", nullable = false)
    private String customerPhone;

    @Column(name = "customer_id_number")
    private String customerIdNumber;

    @Column(name = "rental_start_date", nullable = false)
    private LocalDate rentalStartDate;

    @Column(name = "rental_end_date")
    private LocalDate rentalEndDate;

    @Column(name = "expected_return_date", nullable = false)
    private LocalDate expectedReturnDate;

    @Column(name = "quantity_rented", nullable = false)
    private Integer quantityRented;

    @Column(name = "total_amount", nullable = false)
    private Double totalAmount;

    @Column(name = "deposit_paid", nullable = false)
    private Double depositPaid;

    @Column(name = "status", nullable = false)
    private String status; // PENDING, ACTIVE, COMPLETED, OVERDUE, CANCELLED

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "returned_at")
    private LocalDateTime returnedAt;

    @Column(name = "notes")
    private String notes;
    @Column(name = "amount_paid", nullable = false)
    private Double amountPaid = 0.0;

    @Column(name = "balance_due", nullable = false)
    private Double balanceDue = 0.0;

    @Column(name = "return_condition")
    private String returnCondition; // EXCELLENT, GOOD, DAMAGED, LOST

    @Column(name = "damage_charges")
    private Double damageCharges = 0.0;

    @Column(name = "late_fees")
    private Double lateFees = 0.0;

    @Column(name = "refund_amount")
    private Double refundAmount = 0.0;

    @OneToMany(mappedBy = "rentalTransaction", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<RentalPayment> payments = new ArrayList<>();
    // Constructors
    public RentalTransaction() {
        this.createdAt = LocalDateTime.now();
        this.status = "PENDING";
    }


}