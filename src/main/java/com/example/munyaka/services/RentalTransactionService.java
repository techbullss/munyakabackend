package com.example.munyaka.services;

import com.example.munyaka.repository.RentalItemRepository;
import com.example.munyaka.repository.RentalPaymentRepository;
import com.example.munyaka.repository.RentalTransactionRepository;
import com.example.munyaka.tables.RentalItem;
import com.example.munyaka.tables.RentalPayment;
import com.example.munyaka.tables.RentalTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class RentalTransactionService {

    @Autowired
    private RentalTransactionRepository rentalTransactionRepository;

    @Autowired
    private RentalItemRepository rentalItemRepository;

    @Autowired
    private RentalPaymentRepository rentalPaymentRepository;

    public List<RentalTransaction> getAllRentalTransactions() {
        // Sort by ID in descending order (newest first)
        return rentalTransactionRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }

    public Optional<RentalTransaction> getRentalTransactionById(Long id) {
        return rentalTransactionRepository.findById(id);
    }

    public List<RentalTransaction> getRentalTransactionsByStatus(String status) {
        return rentalTransactionRepository.findByStatus(status);
    }

    public List<RentalTransaction> getOverdueRentals() {
        return rentalTransactionRepository.findOverdueRentals(LocalDate.now());
    }

    public List<RentalTransaction> getCustomerRentalHistory(String phoneNumber) {
        return rentalTransactionRepository.findByCustomerPhone(phoneNumber);
    }

    public List<RentalPayment> getRentalPayments(Long rentalId) {
        return rentalPaymentRepository.findByRentalTransactionIdOrderByPaymentDateDesc(rentalId);
    }

    public Double calculateBalanceDue(Long rentalId) {
        RentalTransaction rental = rentalTransactionRepository.findById(rentalId)
                .orElseThrow(() -> new RuntimeException("Rental transaction not found"));
        return rental.getBalanceDue();
    }

    @Transactional
    public RentalTransaction createRentalTransaction(RentalTransaction rentalTransaction) {
        RentalItem item = rentalTransaction.getRentalItem();

        // Validate that the item exists and has sufficient quantity
        if (item == null) {
            throw new RuntimeException("Rental item not found");
        }

        if (item.getAvailableQuantity() < rentalTransaction.getQuantityRented()) {
            throw new RuntimeException("Insufficient quantity available. Available: " + item.getAvailableQuantity() + ", Requested: " + rentalTransaction.getQuantityRented());
        }

        if (!item.getIsActive()) {
            throw new RuntimeException("Item is not available for rental");
        }

        // Calculate total amount
        long days = ChronoUnit.DAYS.between(
                rentalTransaction.getRentalStartDate(),
                rentalTransaction.getExpectedReturnDate()
        );

        double totalAmount = item.getDailyRate() * days * rentalTransaction.getQuantityRented();
        rentalTransaction.setTotalAmount(totalAmount);
        rentalTransaction.setBalanceDue(totalAmount); // Initial balance equals total amount
        rentalTransaction.setAmountPaid(0.0); // Initialize paid amount to 0
        rentalTransaction.setDepositPaid(0.0); // Initialize deposit paid

        // Update available quantity
        item.setAvailableQuantity(item.getAvailableQuantity() - rentalTransaction.getQuantityRented());
        rentalItemRepository.save(item);

        // Set status to ACTIVE since the item is now rented
        rentalTransaction.setStatus("PENDING");
        rentalTransaction.setCreatedAt(LocalDateTime.now());

        return rentalTransactionRepository.save(rentalTransaction);
    }

    @Transactional
    public RentalPayment processPayment(Long rentalId, RentalPayment payment) {
        RentalTransaction rental = rentalTransactionRepository.findById(rentalId)
                .orElseThrow(() -> new RuntimeException("Rental transaction not found"));

        // Validate payment amount
        if (payment.getAmount() <= 0) {
            throw new RuntimeException("Payment amount must be greater than 0");
        }

        if (payment.getAmount() > rental.getBalanceDue()) {
            throw new RuntimeException("Payment amount cannot exceed balance due: KES " + rental.getBalanceDue());
        }

        // Set payment details
        payment.setRentalTransaction(rental);
        payment.setPaymentDate(LocalDateTime.now());

        if (payment.getIsDeposit() == null) {
            payment.setIsDeposit(false);
        }

        // Update rental payment totals
        rental.setAmountPaid(rental.getAmountPaid() + payment.getAmount());
        rental.setBalanceDue(rental.getTotalAmount() - rental.getAmountPaid());

        // Update deposit paid if this is a deposit payment
        if (Boolean.TRUE.equals(payment.getIsDeposit())) {
            rental.setDepositPaid(rental.getDepositPaid() + payment.getAmount());
        }

        // Save payment first
        RentalPayment savedPayment = rentalPaymentRepository.save(payment);

        // Check if item has been returned (has return date)
        boolean isItemReturned = rental.getRentalEndDate() != null || rental.getReturnedAt() != null;

        // Update status based on whether item is returned and payment status
        if (rental.getBalanceDue() <= 0) {
            // Full payment made
            if (isItemReturned) {
                // Item already returned - mark as COMPLETED
                rental.setStatus("COMPLETED");
            } else {
                // Item not returned yet - check current status
                if ("PARTIALLY_PAID".equals(rental.getStatus()) || "PENDING".equals(rental.getStatus())) {
                    // Item not returned but full payment made - mark as COMPLETED_PENDING_RETURN
                    rental.setStatus("COMPLETED_PENDING_RETURN");
                } else if ("COMPLETED_PENDING_PAYMENT".equals(rental.getStatus())) {
                    // Item returned but was pending payment - now mark as COMPLETED
                    rental.setStatus("COMPLETED");
                } else {
                    // For any other status, mark as PAID (item not returned yet)
                    rental.setStatus("PAID");
                }
            }
        } else {
            // Partial payment made
            if (rental.getAmountPaid() > 0) {
                if (isItemReturned) {
                    // Item returned but partial payment - keep as COMPLETED_PENDING_PAYMENT
                    if (!"COMPLETED_PENDING_PAYMENT".equals(rental.getStatus())) {
                        rental.setStatus("COMPLETED_PENDING_PAYMENT");
                    }
                } else {
                    // Item not returned and partial payment
                    rental.setStatus("PARTIALLY_PAID");
                }
            }
        }

        // Save updated rental
        rentalTransactionRepository.save(rental);

        return savedPayment;
    }

    @Transactional
    public RentalTransaction returnRentalItem(Long rentalId, String returnCondition, Double damageCharges, String notes) {
        RentalTransaction rental = rentalTransactionRepository.findById(rentalId)
                .orElseThrow(() -> new RuntimeException("Rental transaction not found"));

        RentalItem item = rental.getRentalItem();
        LocalDate returnDate = LocalDate.now();

        // Validate return condition
        if (returnCondition == null || returnCondition.trim().isEmpty()) {
            throw new RuntimeException("Return condition is required");
        }

        // Check if item is already returned
        if (rental.getRentalEndDate() != null || rental.getReturnedAt() != null) {
            throw new RuntimeException("This rental item has already been returned on " + rental.getRentalEndDate());
        }

        // Calculate late fees if returned after expected date
        long daysLate = ChronoUnit.DAYS.between(rental.getExpectedReturnDate(), returnDate);
        double lateFees = daysLate > 0 ? daysLate * item.getDailyRate() * 0.5 : 0; // 50% of daily rate as late fee

        // Validate damage charges
        if (damageCharges == null) {
            damageCharges = 0.0;
        }
        if (damageCharges < 0) {
            throw new RuntimeException("Damage charges cannot be negative");
        }

        // Calculate final amount due including late fees and damage charges
        double additionalCharges = lateFees + damageCharges;
        double finalAmountDue = rental.getTotalAmount() + additionalCharges;
        double amountOwed = finalAmountDue - rental.getAmountPaid();

        // Calculate refund (if overpaid) - refund can't exceed amount paid minus deposit
        double refundAmount = 0;
        if (rental.getAmountPaid() > finalAmountDue) {
            refundAmount = rental.getAmountPaid() - finalAmountDue;
            // Ensure refund doesn't exceed non-deposit payments
            double maxRefund = rental.getAmountPaid() - rental.getDepositPaid();
            refundAmount = Math.min(refundAmount, maxRefund);
        }

        // Update rental transaction with return details
        rental.setRentalEndDate(returnDate);
        rental.setReturnedAt(LocalDateTime.now());
        rental.setReturnCondition(returnCondition);
        rental.setDamageCharges(damageCharges);
        rental.setLateFees(lateFees);
        rental.setRefundAmount(refundAmount);
        rental.setBalanceDue(Math.max(0, amountOwed));

        // RESTORE ITEM QUANTITY - Only if item is not lost
        if (!"LOST".equals(returnCondition)) {
            item.setAvailableQuantity(item.getAvailableQuantity() + rental.getQuantityRented());
            rentalItemRepository.save(item);
        } else {
            // If item is lost, mark it as inactive
            item.setIsActive(false);
            item.setAvailableQuantity(0);
            rentalItemRepository.save(item);
        }

        // Determine final status based on payment situation
        String newStatus;
        if (amountOwed > 0) {
            newStatus = "COMPLETED_PENDING_PAYMENT"; // Returned but still owes money
        } else if (refundAmount > 0) {
            newStatus = "COMPLETED_REFUND"; // Returned and refund due
        } else {
            newStatus = "COMPLETED"; // Fully settled
        }

        rental.setStatus(newStatus);

        // Update notes if provided
        if (notes != null && !notes.trim().isEmpty()) {
            rental.setNotes(notes);
        }

        return rentalTransactionRepository.save(rental);
    }

    // NEW METHOD: Process payment for already returned items
    @Transactional
    public RentalPayment processPaymentAfterReturn(Long rentalId, RentalPayment payment) {
        RentalTransaction rental = rentalTransactionRepository.findById(rentalId)
                .orElseThrow(() -> new RuntimeException("Rental transaction not found"));

        // Only allow payments for returned items that have balance due
        if (!rental.getStatus().equals("COMPLETED_PENDING_PAYMENT")) {
            throw new RuntimeException("Payment after return only allowed for returned items with pending payment");
        }

        if (rental.getBalanceDue() <= 0) {
            throw new RuntimeException("No balance due for this rental");
        }

        if (payment.getAmount() <= 0) {
            throw new RuntimeException("Payment amount must be greater than 0");
        }

        if (payment.getAmount() > rental.getBalanceDue()) {
            throw new RuntimeException("Payment amount cannot exceed balance due: KES " + rental.getBalanceDue());
        }

        // Set payment details
        payment.setRentalTransaction(rental);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setIsDeposit(false); // Payments after return are not deposits

        // Update payment totals
        rental.setAmountPaid(rental.getAmountPaid() + payment.getAmount());
        rental.setBalanceDue(rental.getBalanceDue() - payment.getAmount());

        // Update status if balance is now fully paid
        if (rental.getBalanceDue() <= 0) {
            rental.setStatus("COMPLETED");
        }

        // Save payment and update rental
        RentalPayment savedPayment = rentalPaymentRepository.save(payment);
        rentalTransactionRepository.save(rental);

        return savedPayment;
    }

    public void deleteRentalTransaction(Long id) {
        RentalTransaction rentalTransaction = rentalTransactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rental transaction not found"));

        // Add any business logic checks here (e.g., only allow deletion of certain statuses)
        if (!rentalTransaction.getStatus().equals("CANCELLED") &&
                !rentalTransaction.getStatus().equals("COMPLETED")) {
            throw new RuntimeException("Only cancelled or completed rentals can be deleted");
        }

        rentalTransactionRepository.delete(rentalTransaction);
    }

    @Transactional
    public RentalTransaction cancelRental(Long rentalId, String reason) {
        RentalTransaction rental = rentalTransactionRepository.findById(rentalId)
                .orElseThrow(() -> new RuntimeException("Rental transaction not found"));

        // Only allow cancellation of active rentals
        if (!"PENDING".equals(rental.getStatus()) && !"PARTIALLY_PAID".equals(rental.getStatus())) {
            throw new RuntimeException("Cannot cancel rental with status: " + rental.getStatus());
        }

        // Restore item quantity
        RentalItem item = rental.getRentalItem();
        item.setAvailableQuantity(item.getAvailableQuantity() + rental.getQuantityRented());
        rentalItemRepository.save(item);

        // Process refund if any amount was paid (excluding deposit)
        if (rental.getAmountPaid() > 0) {
            double refundableAmount = rental.getAmountPaid() - rental.getDepositPaid();
            if (refundableAmount > 0) {
                rental.setRefundAmount(refundableAmount);
            }
        }

        // Update rental status
        rental.setStatus("CANCELLED");
        rental.setRentalEndDate(LocalDate.now());
        rental.setReturnedAt(LocalDateTime.now());
        rental.setNotes("Cancelled: " + (reason != null ? reason : "No reason provided"));

        return rentalTransactionRepository.save(rental);
    }

    public List<RentalTransaction> getRentalsByCustomerPhone(String phoneNumber) {
        return rentalTransactionRepository.findByCustomerPhone(phoneNumber);
    }

    public List<RentalTransaction> getActiveRentals() {
        return rentalTransactionRepository.findByStatus("ACTIVE");
    }

    public List<RentalTransaction> getPendingPaymentRentals() {
        return rentalTransactionRepository.findByStatus("PARTIALLY_PAID");
    }

    public List<RentalTransaction> getCompletedPendingPaymentRentals() {
        return rentalTransactionRepository.findByStatus("COMPLETED_PENDING_PAYMENT");
    }

    public RentalTransaction updateRentalStatus(Long id, String newStatus) {
        RentalTransaction rental = rentalTransactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rental transaction not found"));

        // Validate status transition
        if (isValidStatusTransition(rental.getStatus(), newStatus)) {
            rental.setStatus(newStatus);
            return rentalTransactionRepository.save(rental);
        } else {
            throw new RuntimeException("Invalid status transition from " + rental.getStatus() + " to " + newStatus);
        }
    }

    private boolean isValidStatusTransition(String currentStatus, String newStatus) {
        // Define your valid status transitions
        Map<String, List<String>> validTransitions = Map.of(
                "PENDING", Arrays.asList("PAID", "CANCELLED"),
                "PAID", Arrays.asList("COMPLETED_PENDING_RETURN", "COMPLETED_PENDING_PAYMENT", "COMPLETED", "OVERDUE"),
                "COMPLETED_PENDING_RETURN", Arrays.asList("COMPLETED"),
                "COMPLETED_PENDING_PAYMENT", Arrays.asList("COMPLETED")
        );

        return validTransitions.getOrDefault(currentStatus, new ArrayList<>()).contains(newStatus);
    }
}