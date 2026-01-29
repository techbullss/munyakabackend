package com.example.munyaka.controllers;

import com.example.munyaka.tables.RentalPayment;
import com.example.munyaka.tables.RentalTransaction;
import com.example.munyaka.services.RentalTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/rental-transactions")
@CrossOrigin(origins = "http://localhost:3000")
public class RentalTransactionController {

    @Autowired
    private RentalTransactionService rentalTransactionService;

    @GetMapping
    public List<RentalTransaction> getAllRentalTransactions() {
        return rentalTransactionService.getAllRentalTransactions();
    }

    @GetMapping("/{id}")
    public ResponseEntity<RentalTransaction> getRentalTransactionById(@PathVariable Long id) {
        Optional<RentalTransaction> rentalTransaction = rentalTransactionService.getRentalTransactionById(id);
        return rentalTransaction.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/status/{status}")
    public List<RentalTransaction> getRentalTransactionsByStatus(@PathVariable String status) {
        return rentalTransactionService.getRentalTransactionsByStatus(status);
    }

    @GetMapping("/overdue")
    public List<RentalTransaction> getOverdueRentals() {
        return rentalTransactionService.getOverdueRentals();
    }

    @GetMapping("/customer/{phoneNumber}")
    public List<RentalTransaction> getCustomerRentalHistory(@PathVariable String phoneNumber) {
        return rentalTransactionService.getCustomerRentalHistory(phoneNumber);
    }

    @PostMapping
    public ResponseEntity<RentalTransaction> createRentalTransaction(@RequestBody RentalTransaction rentalTransaction) {
        try {
            RentalTransaction createdRental = rentalTransactionService.createRentalTransaction(rentalTransaction);
            return ResponseEntity.ok(createdRental);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // Payment endpoint - FIXED to use RentalPayment object
    @PostMapping("/{id}/payments")
    public ResponseEntity<?> processPayment(@PathVariable Long id, @RequestBody RentalPayment payment) {
        try {
            RentalPayment processedPayment = rentalTransactionService.processPayment(id, payment);
            return ResponseEntity.ok(processedPayment);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}/payments")
    public ResponseEntity<?> getRentalPayments(@PathVariable Long id) {
        try {
            List<RentalPayment> payments = rentalTransactionService.getRentalPayments(id);
            return ResponseEntity.ok(payments);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/balance")
    public ResponseEntity<?> getBalanceDue(@PathVariable Long id) {
        try {
            Double balance = rentalTransactionService.calculateBalanceDue(id);
            return ResponseEntity.ok(Map.of("balanceDue", balance));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Return item endpoint - using request body
    @PutMapping("/{id}/return")
    public ResponseEntity<?> returnRentalItem(
            @PathVariable Long id,
            @RequestBody Map<String, Object> returnRequest) {

        try {
            String returnCondition = (String) returnRequest.get("returnCondition");
            Double damageCharges = returnRequest.get("damageCharges") != null ?
                    Double.valueOf(returnRequest.get("damageCharges").toString()) : 0.0;
            String notes = (String) returnRequest.get("notes");

            RentalTransaction returnedRental = rentalTransactionService.returnRentalItem(
                    id, returnCondition, damageCharges, notes);
            return ResponseEntity.ok(returnedRental);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Cancel rental endpoint
    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelRental(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {

        try {
            RentalTransaction cancelledRental = rentalTransactionService.cancelRental(id, reason);
            return ResponseEntity.ok(cancelledRental);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Additional endpoints
    @GetMapping("/customer/{phoneNumber}/history")
    public List<RentalTransaction> getRentalsByCustomerPhone(@PathVariable String phoneNumber) {
        return rentalTransactionService.getRentalsByCustomerPhone(phoneNumber);
    }

    @GetMapping("/active")
    public List<RentalTransaction> getActiveRentals() {
        return rentalTransactionService.getActiveRentals();
    }

    @GetMapping("/pending-payment")
    public List<RentalTransaction> getPendingPaymentRentals() {
        return rentalTransactionService.getPendingPaymentRentals();
    }
    // Add this method to your RentalTransactionController
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateRentalStatus(@PathVariable Long id, @RequestBody Map<String, String> statusUpdate) {
        try {
            String newStatus = statusUpdate.get("status");
            RentalTransaction updatedTransaction = rentalTransactionService.updateRentalStatus(id, newStatus);
            return ResponseEntity.ok(updatedTransaction);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRentalTransaction(@PathVariable Long id) {
        try {
            rentalTransactionService.deleteRentalTransaction(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}