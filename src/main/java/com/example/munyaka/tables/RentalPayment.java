package com.example.munyaka.tables;


import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "rental_payments")
public class RentalPayment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "rental_transaction_id", nullable = false)
    private RentalTransaction rentalTransaction;

    @Column(name = "amount", nullable = false)
    private Double amount;

    @Column(name = "payment_type", nullable = false)
    private String paymentType; // CASH, MPESA, BANK_TRANSFER, CARD

    @Column(name = "payment_date", nullable = false)
    private LocalDateTime paymentDate;

    @Column(name = "reference_number")
    private String referenceNumber;

    @Column(name = "notes")
    private String notes;

    @Column(name = "is_deposit", nullable = false)
    private Boolean isDeposit = false;

    // Constructors
    public RentalPayment() {
        this.paymentDate = LocalDateTime.now();
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public RentalTransaction getRentalTransaction() { return rentalTransaction; }
    public void setRentalTransaction(RentalTransaction rentalTransaction) { this.rentalTransaction = rentalTransaction; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public String getPaymentType() { return paymentType; }
    public void setPaymentType(String paymentType) { this.paymentType = paymentType; }

    public LocalDateTime getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDateTime paymentDate) { this.paymentDate = paymentDate; }

    public String getReferenceNumber() { return referenceNumber; }
    public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Boolean getIsDeposit() { return isDeposit; }
    public void setIsDeposit(Boolean isDeposit) { this.isDeposit = isDeposit; }
}
