package com.example.munyaka.controllers;

import com.example.munyaka.DTO.ReturnItemDTO;
import com.example.munyaka.repository.ItemRepository;
import com.example.munyaka.repository.SaleRepository;
import com.example.munyaka.tables.Item;
import com.example.munyaka.tables.Sale;
import com.example.munyaka.tables.SaleItem;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SaleService {

    @Autowired
    SaleRepository saleRepository;

    @Autowired
    ItemRepository itemRepository;

    @Transactional
    public void processReturn(Long saleId, List<ReturnItemDTO> items) {
        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new RuntimeException("Sale not found"));

        for (ReturnItemDTO returnItem : items) {
            SaleItem saleItem = sale.getItems().stream()
                    .filter(i -> i.getProduct().getId().equals(returnItem.getProductId()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Item not found in sale"));

            if (returnItem.getQuantity() > saleItem.getQuantity()) {
                throw new RuntimeException(
                        "Return quantity exceeds sold quantity for product " + returnItem.getProductId()
                );
            }

            int originalQty = saleItem.getQuantity();
            int returnQty = returnItem.getQuantity();
            Item product = saleItem.getProduct();

            double unitCostPrice = product.getPrice();
            double unitSellingPrice = product.getSellingPrice();
            double unitProfit = unitSellingPrice - unitCostPrice;

            // Apply return - reduce quantity sold
            int newQty = originalQty - returnQty;
            saleItem.setQuantity(newQty);

            // Calculate new totals for this item
            double newItemTotal = newQty * unitSellingPrice;
            double newItemProfit = newQty * unitProfit;

            saleItem.setTotal(newItemTotal);
            saleItem.setProfit(newItemProfit);

            // Add notes about the return
            if (returnItem.getReason() != null && !returnItem.getReason().isEmpty()) {
                String oldNotes = saleItem.getNotes() == null ? "" : saleItem.getNotes() + " | ";
                saleItem.setNotes(
                        oldNotes + "Returned " + returnQty +
                                " (" + returnItem.getReason() +
                                ", " + returnItem.getCondition() + ")"
                );
            }

            // Restore stock
            product.setStockQuantity(product.getStockQuantity() + returnQty);
            itemRepository.save(product);
        }

        // Recalculate ENTIRE sale totals - include ALL sale items
        double newTotal = sale.getItems().stream()
                .mapToDouble(i -> {
                    // Calculate total based on current quantity and unit price
                    Item product = i.getProduct();
                    double unitSellingPrice = product.getSellingPrice();
                    return i.getQuantity() * unitSellingPrice;
                })
                .sum();
        sale.setTotalAmount(newTotal);

        // Recalculate ENTIRE sale profit - include ALL sale items
        double newProfit = sale.getItems().stream()
                .mapToDouble(i -> {
                    // Calculate profit based on current quantity and unit profit
                    Item product = i.getProduct();
                    double unitProfit = product.getSellingPrice() - product.getPrice();
                    return i.getQuantity() * unitProfit;
                })
                .sum();
        sale.setProfit(newProfit);

        // Recalculate balance
        double paid = sale.getPaidAmount() == null ? 0.0 : sale.getPaidAmount();
        double newBalance = newTotal - paid;
        if (newBalance < 0) newBalance = 0.0;
        sale.setBalanceDue(newBalance);

        // Payment status logic
        boolean allItemsReturned = sale.getItems().stream().allMatch(i -> i.getQuantity() == 0);

        if (allItemsReturned) {
            sale.setTotalAmount(0.0);
            sale.setBalanceDue(0.0);
            sale.setProfit(0.0);
            sale.setPaymentStatus("GOODS_RETURNED");
        } else if (newBalance == 0.0) {
            sale.setPaymentStatus("PAID");
        } else if (newBalance > 0 && paid > 0) {
            sale.setPaymentStatus("PARTIALLY_PAID");
        } else {
            sale.setPaymentStatus("UNPAID");
        }

        saleRepository.save(sale);
    }
}