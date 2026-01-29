package com.example.munyaka.controllers;

import com.example.munyaka.DTO.*;
import com.example.munyaka.repository.ItemRepository;
import com.example.munyaka.repository.SaleRepository;
import com.example.munyaka.tables.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000/")
public class SaleController {
    private final SaleRepository repo;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private SaleService saleService;

    // CREATE SALE

    @PostMapping
    public SaleReceiptResponse createSale(@RequestBody SaleRequest request) {
        Sale sale = new Sale();
        sale.setCustomerName(request.getCustomerName());
        sale.setSaleDate(LocalDate.now());
        sale.setTotalAmount(request.getTotalAmount());
        sale.setBalanceDue(request.getChangeAmount());
        sale.setPaidAmount(request.getAmountPaid());
        sale.setPaymentMethod(request.getPaymentMethod());
        sale.setCustomerPhone(request.getCustomerPhone());
        sale.setIsDeleted(false);
        sale.setDeletedAt(null);

        List<SaleItem> saleItems = new ArrayList<>();
        double total = 0.0;
        double totalProfit = 0.0;
        List<ReceiptItem> receiptItems = new ArrayList<>();

        for (SaleItemRequest reqItem : request.getItems()) {
            Item product = itemRepository.findById(reqItem.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            SaleItem saleItem = new SaleItem();
            saleItem.setSale(sale);
            saleItem.setProduct(product);
            saleItem.setQuantity(reqItem.getQuantity());
            saleItem.setTotal(reqItem.getTotal());
            saleItem.setDiscountAmount(reqItem.getDiscountAmount());
            saleItem.setUnitPrice(Double.valueOf(reqItem.getPrice())); // ADD THIS LINE - store edited price
            saleItems.add(saleItem);

            // Use the price from request (cart) not from product
            total += reqItem.getTotal(); // Already includes discount

            // Profit calculation per item - use cart price for calculation
            double sellingPrice = reqItem.getPrice() != null ? reqItem.getPrice() : product.getSellingPrice();
            double profit = (sellingPrice - product.getPrice() - (reqItem.getDiscountAmount() != null ? reqItem.getDiscountAmount() : 0.0))
                    * reqItem.getQuantity();
            totalProfit += profit;

            // Build receipt item - use cart price
            ReceiptItem dto = new ReceiptItem();
            dto.setProductName(product.getItemName());
            dto.setQuantity(reqItem.getQuantity());
            dto.setUnitPrice(sellingPrice); // Use cart price
            dto.setLineTotal(reqItem.getTotal());
            dto.setProfit(profit);
            receiptItems.add(dto);
        }

        sale.setItems(saleItems);
        sale.setProfit(totalProfit);
        sale.setTotalAmount(total); // Set the calculated total

        // Determine payment status
        double paidAmount = request.getAmountPaid();

        // Status logic
        if (paidAmount == 0) {
            sale.setPaymentStatus("PENDING");
        } else if (paidAmount < total) {
            sale.setPaymentStatus("PARTIAL");
        } else if (paidAmount == total) {
            sale.setPaymentStatus("PAID");
        } else {
            sale.setPaymentStatus("OVERPAID");
        }

        saleRepository.save(sale);

        // Build receipt response
        SaleReceiptResponse response = new SaleReceiptResponse();
        response.setCustomerName(sale.getCustomerName());
        response.setSaleDate(String.valueOf(sale.getSaleDate()));
        response.setTotalAmount(total);
        response.setPaidAmount(request.getAmountPaid());
        response.setChange(request.getAmountPaid() - total);
        response.setPaymentMethod(request.getPaymentMethod());
        response.setProfit(totalProfit);
        response.setItems(receiptItems);

        return response;
    }

    // UPDATE SALE WITH ITEMS - COMPLETE EDIT FUNCTIONALITY
    @PutMapping("/{id}/edit")
    @Transactional
    public ResponseEntity<?> editSaleWithItems(
            @PathVariable Long id,
            @RequestBody SaleEditRequest request) {

        try {
            Optional<Sale> saleOpt = saleRepository.findActiveById(id);

            if (saleOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "success", false,
                                "message", "Sale not found or has been deleted"
                        ));
            }

            Sale existingSale = saleOpt.get();

            // Update basic customer info
            if (request.getCustomerName() != null) {
                existingSale.setCustomerName(request.getCustomerName());
            }
            if (request.getCustomerPhone() != null) {
                existingSale.setCustomerPhone(request.getCustomerPhone());
            }
            if (request.getPaymentMethod() != null) {
                existingSale.setPaymentMethod(request.getPaymentMethod());
            }
            if (request.getSaleDate() != null) {
                existingSale.setSaleDate(LocalDate.parse(request.getSaleDate()));
            }

            // Update sale items if provided
            if (request.getItems() != null && !request.getItems().isEmpty()) {
                // Clear existing items first
                existingSale.getItems().clear();

                double newTotal = 0.0;
                double newProfit = 0.0;

                // Add new/updated items
                // Inside the for loop in editSaleWithItems method:
                for (SaleItemRequest itemRequest : request.getItems()) {
                    Item product = itemRepository.findById(itemRequest.getProductId())
                            .orElseThrow(() -> new RuntimeException(
                                    "Product not found: " + itemRequest.getProductId()));

                    SaleItem saleItem = new SaleItem();
                    saleItem.setSale(existingSale);
                    saleItem.setProduct(product);
                    saleItem.setQuantity(itemRequest.getQuantity());
                    saleItem.setTotal(itemRequest.getTotal());

                    // Store the edited price from request
                    saleItem.setUnitPrice(itemRequest.getPrice() != null ?
                            itemRequest.getPrice() : product.getSellingPrice());

                    saleItem.setDiscountAmount(itemRequest.getDiscountAmount() != null ?
                            itemRequest.getDiscountAmount() : 0.0);

                    existingSale.getItems().add(saleItem);

                    // Calculate new totals
                    newTotal += itemRequest.getTotal();

                    // Calculate profit for this item - use cart price (edited price)
                    double sellingPrice = itemRequest.getPrice() != null ?
                            itemRequest.getPrice() : product.getSellingPrice();
                    double discountAmount = itemRequest.getDiscountAmount() != null ?
                            itemRequest.getDiscountAmount() : 0.0;
                    double discountPerUnit = discountAmount / itemRequest.getQuantity();

                    // Profit calculation: (selling price - buying price - discount per unit) * quantity
                    double profit = (sellingPrice - product.getPrice() - discountPerUnit) * itemRequest.getQuantity();
                    newProfit += profit;
                }

                existingSale.setTotalAmount(newTotal);
                existingSale.setProfit(newProfit);
            }

            // Update payment information if provided
            if (request.getPaidAmount() != null) {
                double totalAmount = existingSale.getTotalAmount() != null ?
                        existingSale.getTotalAmount() : 0.0;
                double paidAmount = request.getPaidAmount();
                double balance = totalAmount - paidAmount;

                existingSale.setPaidAmount(paidAmount);
                existingSale.setBalanceDue(balance);

                // Update payment status based on new payment
                if (paidAmount == 0) {
                    existingSale.setPaymentStatus("PENDING");
                } else if (paidAmount < totalAmount) {
                    existingSale.setPaymentStatus("PARTIAL");
                } else if (paidAmount == totalAmount) {
                    existingSale.setPaymentStatus("PAID");
                } else {
                    existingSale.setPaymentStatus("OVERPAID");
                }
            }

            // Save the updated sale
            Sale savedSale = saleRepository.save(existingSale);

            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Sale updated successfully");
            response.put("id", savedSale.getId());
            response.put("totalAmount", savedSale.getTotalAmount());
            response.put("profit", savedSale.getProfit());
            response.put("paidAmount", savedSale.getPaidAmount());
            response.put("balanceDue", savedSale.getBalanceDue());
            response.put("paymentStatus", savedSale.getPaymentStatus());
            response.put("itemsCount", savedSale.getItems().size());

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "success", false,
                            "message", e.getMessage()
                    ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "Failed to update sale: " + e.getMessage()
                    ));
        }
    }
    // UPDATE BASIC SALE INFO (without items)
    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> updateSale(
            @PathVariable Long id,
            @RequestBody SaleUpdateRequest request) {

        try {
            Optional<Sale> saleOpt = saleRepository.findActiveById(id);

            if (saleOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "success", false,
                                "message", "Sale not found or has been deleted"
                        ));
            }

            Sale existingSale = saleOpt.get();

            // Update only allowed fields
            if (request.getCustomerName() != null) {
                existingSale.setCustomerName(request.getCustomerName());
            }
            if (request.getCustomerPhone() != null) {
                existingSale.setCustomerPhone(request.getCustomerPhone());
            }
            if (request.getPaymentMethod() != null) {
                existingSale.setPaymentMethod(request.getPaymentMethod());
            }
            if (request.getSaleDate() != null) {
                existingSale.setSaleDate(LocalDate.parse(request.getSaleDate()));
            }
            if (request.getPaidAmount() != null) {
                double totalAmount = existingSale.getTotalAmount() != null ?
                        existingSale.getTotalAmount() : 0.0;
                double paidAmount = request.getPaidAmount();
                double balance = totalAmount - paidAmount;

                existingSale.setPaidAmount(paidAmount);
                existingSale.setBalanceDue(balance);

                // Update payment status
                if (paidAmount == 0) {
                    existingSale.setPaymentStatus("PENDING");
                } else if (paidAmount < totalAmount) {
                    existingSale.setPaymentStatus("PARTIAL");
                } else if (paidAmount == totalAmount) {
                    existingSale.setPaymentStatus("PAID");
                } else {
                    existingSale.setPaymentStatus("OVERPAID");
                }
            }

            Sale savedSale = saleRepository.save(existingSale);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Sale updated successfully");
            response.put("id", savedSale.getId());
            response.put("customerName", savedSale.getCustomerName());
            response.put("customerPhone", savedSale.getCustomerPhone());
            response.put("paymentMethod", savedSale.getPaymentMethod());
            response.put("paidAmount", savedSale.getPaidAmount());
            response.put("balanceDue", savedSale.getBalanceDue());
            response.put("paymentStatus", savedSale.getPaymentStatus());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "Failed to update sale: " + e.getMessage()
                    ));
        }
    }

    // GET SALE BY ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getSale(@PathVariable Long id) {
        try {
            Optional<Sale> saleOpt = saleRepository.findActiveById(id);

            if (saleOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "success", false,
                                "message", "Sale not found or has been deleted"
                        ));
            }

            Sale sale = saleOpt.get();
            return ResponseEntity.ok(new SaleSummaryDTO(sale));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "Failed to fetch sale: " + e.getMessage()
                    ));
        }
    }

    // GET SALES BY PERIOD WITH FILTER
    @GetMapping("/filter")
    public ResponseEntity<?> getSalesByPeriod(
            @RequestParam String start,
            @RequestParam String end,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "saleDate,desc") String sort) {

        try {
            LocalDate startDate = LocalDate.parse(start);
            LocalDate endDate = LocalDate.parse(end);

            // Parse sort param
            String[] sortParams = sort.split(",");
            String sortField = sortParams[0];
            Sort.Direction direction = (sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc"))
                    ? Sort.Direction.ASC
                    : Sort.Direction.DESC;

            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

            // Fetch paginated sales (for table display) - ACTIVE ONLY
            Page<Sale> salesPage = saleRepository.findActiveBySaleDateBetween(startDate, endDate, pageable);
            List<SaleSummaryDTO> saleDTOs = salesPage.getContent()
                    .stream()
                    .map(SaleSummaryDTO::new)
                    .collect(Collectors.toList());

            // Fetch all sales (for summary calculations, not paginated) - ACTIVE ONLY
            List<Sale> allSales = saleRepository.findAllActiveBySaleDateBetween(startDate, endDate);
            List<SaleSummaryDTO> allSaleDTOs = allSales.stream()
                    .map(SaleSummaryDTO::new)
                    .collect(Collectors.toList());

            // Summary calculations
            double totalAmount = allSaleDTOs.stream()
                    .mapToDouble(SaleSummaryDTO::getTotalAmount)
                    .sum();
            int totalSales = allSaleDTOs.size();
            double avgSale = totalSales > 0 ? totalAmount / totalSales : 0;
            int totalItems = allSaleDTOs.stream()
                    .mapToInt(s -> s.getItems().size())
                    .sum();
            double totalProfit = allSaleDTOs.stream()
                    .mapToDouble(SaleSummaryDTO::getProfit)
                    .sum();

            // Previous period deviation
            long daysBetween = java.time.Duration.between(
                    startDate.atStartOfDay(),
                    endDate.atStartOfDay()
            ).toDays() + 1;

            LocalDate prevStart = startDate.minusDays(daysBetween);
            LocalDate prevEnd = startDate.minusDays(1);

            double prevAmount = saleRepository.findAllActiveBySaleDateBetween(prevStart, prevEnd)
                    .stream()
                    .mapToDouble(s -> s.getTotalAmount() != null ? s.getTotalAmount() : 0.0)
                    .sum();

            double deviation = prevAmount == 0
                    ? 100
                    : ((totalAmount - prevAmount) / prevAmount) * 100;

            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("sales", saleDTOs);
            response.put("summary", Map.of(
                    "totalSales", totalSales,
                    "totalAmount", totalAmount,
                    "totalProfit", totalProfit,
                    "averageSale", avgSale,
                    "totalItems", totalItems,
                    "deviation", deviation
            ));
            response.put("page", salesPage.getNumber());
            response.put("size", salesPage.getSize());
            response.put("totalElements", salesPage.getTotalElements());
            response.put("totalPages", salesPage.getTotalPages());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "Failed to fetch sales: " + e.getMessage()
                    ));
        }
    }

    // GET ALL SALES WITH PAGINATION
    @GetMapping
    public ResponseEntity<?> getSalesPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "saleDate") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        try {
            Sort sort = direction.equalsIgnoreCase("asc")
                    ? Sort.by(sortBy).ascending()
                    : Sort.by(sortBy).descending();

            Pageable pageable = PageRequest.of(page, size, sort);
            Page<Sale> salePage = saleRepository.findAllActive(pageable);

            double totalProfit = salePage.getContent()
                    .stream()
                    .mapToDouble(Sale::getProfit)
                    .sum();

            List<SaleSummaryDTO> sales = salePage.getContent()
                    .stream()
                    .map(SaleSummaryDTO::new)
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("sales", sales);
            response.put("currentPage", salePage.getNumber());
            response.put("totalItems", salePage.getTotalElements());
            response.put("totalPages", salePage.getTotalPages());
            response.put("totalProfit", totalProfit);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "Failed to fetch sales: " + e.getMessage()
                    ));
        }
    }

    // SOFT DELETE: Mark sale as deleted
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> softDeleteSale(@PathVariable Long id) {
        try {
            // Find the sale first
            Optional<Sale> saleOpt = saleRepository.findActiveById(id);

            if (saleOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "success", false,
                                "message", "Sale not found or already deleted",
                                "id", id
                        ));
            }

            Sale sale = saleOpt.get();
            sale.setIsDeleted(true);
            sale.setDeletedAt(LocalDate.now());
            saleRepository.save(sale);

            return ResponseEntity.ok().body(Map.of(
                    "success", true,
                    "message", "Sale soft deleted successfully",
                    "id", id,
                    "deletedAt", sale.getDeletedAt()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "Failed to delete sale: " + e.getMessage()
                    ));
        }
    }

    // BULK SOFT DELETE
    @DeleteMapping("/bulk")
    @Transactional
    public ResponseEntity<?> softDeleteSales(@RequestBody List<Long> saleIds) {
        try {
            // Verify all sales exist and are active
            List<Sale> sales = saleRepository.findAllById(saleIds);
            List<Long> activeSaleIds = sales.stream()
                    .filter(sale -> !sale.getIsDeleted())
                    .map(Sale::getId)
                    .collect(Collectors.toList());

            if (activeSaleIds.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of(
                                "success", false,
                                "message", "No active sales found to delete"
                        ));
            }

            // Soft delete all
            for (Long id : activeSaleIds) {
                saleRepository.softDelete(id);
            }

            return ResponseEntity.ok().body(Map.of(
                    "success", true,
                    "message", activeSaleIds.size() + " sales soft deleted successfully",
                    "deletedIds", activeSaleIds,
                    "skippedIds", saleIds.stream()
                            .filter(id -> !activeSaleIds.contains(id))
                            .collect(Collectors.toList())
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "Failed to delete sales: " + e.getMessage()
                    ));
        }
    }

    // RESTORE deleted sale
    @PostMapping("/{id}/restore")
    @Transactional
    public ResponseEntity<?> restoreSale(@PathVariable Long id) {
        try {
            Optional<Sale> saleOpt = saleRepository.findById(id);

            if (saleOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "success", false,
                                "message", "Sale not found",
                                "id", id
                        ));
            }

            Sale sale = saleOpt.get();

            if (!sale.getIsDeleted()) {
                return ResponseEntity.badRequest()
                        .body(Map.of(
                                "success", false,
                                "message", "Sale is not deleted",
                                "id", id
                        ));
            }

            sale.setIsDeleted(false);
            sale.setDeletedAt(null);
            saleRepository.save(sale);

            return ResponseEntity.ok().body(Map.of(
                    "success", true,
                    "message", "Sale restored successfully",
                    "id", id
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "Failed to restore sale: " + e.getMessage()
                    ));
        }
    }

    // GET deleted sales (for admin/recovery)
    @GetMapping("/deleted")
    public ResponseEntity<?> getDeletedSales(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("deletedAt").descending());
            Page<Sale> deletedSalesPage = saleRepository.findAllDeleted(pageable);

            List<SaleSummaryDTO> deletedSales = deletedSalesPage.getContent()
                    .stream()
                    .map(sale -> {
                        SaleSummaryDTO dto = new SaleSummaryDTO(sale);
                        dto.setDeletedAt(sale.getDeletedAt());
                        return dto;
                    })
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("deletedSales", deletedSales);
            response.put("currentPage", deletedSalesPage.getNumber());
            response.put("totalItems", deletedSalesPage.getTotalElements());
            response.put("totalPages", deletedSalesPage.getTotalPages());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "Failed to fetch deleted sales: " + e.getMessage()
                    ));
        }
    }

    // PERMANENT DELETE (Admin only - optional)
    @DeleteMapping("/{id}/permanent")
    @Transactional
    public ResponseEntity<?> permanentDeleteSale(@PathVariable Long id) {
        try {
            Optional<Sale> saleOpt = saleRepository.findById(id);

            if (saleOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "success", false,
                                "message", "Sale not found",
                                "id", id
                        ));
            }

            Sale sale = saleOpt.get();

            // Only allow permanent delete if soft deleted for 30+ days
            if (sale.getDeletedAt() != null &&
                    sale.getDeletedAt().plusDays(30).isBefore(LocalDate.now())) {

                saleRepository.deleteById(id);

                return ResponseEntity.ok().body(Map.of(
                        "success", true,
                        "message", "Sale permanently deleted",
                        "id", id,
                        "note", "Sale was soft deleted on " + sale.getDeletedAt()
                ));
            } else if (sale.getIsDeleted()) {
                return ResponseEntity.badRequest()
                        .body(Map.of(
                                "success", false,
                                "message", "Sale must be soft deleted for at least 30 days before permanent deletion",
                                "deletedAt", sale.getDeletedAt()
                        ));
            } else {
                return ResponseEntity.badRequest()
                        .body(Map.of(
                                "success", false,
                                "message", "Sale must be soft deleted first",
                                "isDeleted", sale.getIsDeleted()
                        ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "Failed to permanently delete sale: " + e.getMessage()
                    ));
        }
    }

    // DEBTORS
    @GetMapping("/debtors")
    public ResponseEntity<?> getDebtors() {
        try {
            List<Sale> pendingSales = saleRepository.findActivePendingSales();
            Map<String, DebtorDTO> debtorMap = new HashMap<>();

            for (Sale sale : pendingSales) {
                String customerKey = sale.getCustomerPhone() + "|" + sale.getCustomerName();

                if (!debtorMap.containsKey(customerKey)) {
                    DebtorDTO debtor = DebtorDTO.builder()
                            .id((long) debtorMap.size() + 1)
                            .customerName(sale.getCustomerName())
                            .customerPhone(sale.getCustomerPhone())
                            .totalDebt(0.0)
                            .lastSaleDate(String.valueOf(sale.getSaleDate()))
                            .paymentStatus(calculatePaymentStatus(String.valueOf(sale.getSaleDate())))
                            .sales(new ArrayList<>())
                            .build();
                    debtorMap.put(customerKey, debtor);
                }

                DebtorDTO debtor = debtorMap.get(customerKey);
                debtor.setTotalDebt(debtor.getTotalDebt() + sale.getBalanceDue());
                debtor.getSales().add(convertToSaleDTO(sale));

                // Update last sale date if this sale is more recent
                if (isMoreRecent(String.valueOf(sale.getSaleDate()), debtor.getLastSaleDate())) {
                    debtor.setLastSaleDate(String.valueOf(sale.getSaleDate()));
                }
            }

            return ResponseEntity.ok(new ArrayList<>(debtorMap.values()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "Failed to fetch debtors: " + e.getMessage()
                    ));
        }
    }

    // GET PENDING SALES
    @GetMapping("/pending")
    public ResponseEntity<?> getPendingSales() {
        try {
            List<Sale> pendingSales = saleRepository.findActivePendingSales();
            List<SaleDTO> saleDTOs = pendingSales.stream()
                    .map(this::convertToSaleDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(saleDTOs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "Failed to fetch pending sales: " + e.getMessage()
                    ));
        }
    }

    // RECORD PAYMENT
    @PostMapping("/payment/{id}")
    public ResponseEntity<?> recordPayment(
            @PathVariable Long id,
            @RequestBody PaymentRequest paymentRequest) {

        System.out.println("Received payment for sale ID: " + id +
                ", Amount: " + paymentRequest.getPaymentAmount());

        try {
            Optional<Sale> saleOpt = saleRepository.findActiveById(id);

            if (saleOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "success", false,
                                "message", "Sale not found or has been deleted"
                        ));
            }

            Sale sale = saleOpt.get();
            Double paymentAmount = paymentRequest.getPaymentAmount();

            if (paymentAmount <= 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of(
                                "success", false,
                                "message", "Payment amount must be greater than zero"
                        ));
            }

            // Update paid amount and balance due
            Double newPaidAmount = sale.getPaidAmount() + paymentAmount;
            Double newBalanceDue = sale.getTotalAmount() - newPaidAmount;

            sale.setPaidAmount(newPaidAmount);
            sale.setBalanceDue(newBalanceDue);

            // Update payment status
            if (newBalanceDue <= 0) {
                sale.setPaymentStatus("PAID");
                sale.setChangeAmount(Math.abs(newBalanceDue));
            } else {
                sale.setPaymentStatus("PENDING");
            }

            Sale updatedSale = saleRepository.save(sale);
            return ResponseEntity.ok(convertToSaleDTO(updatedSale));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "Failed to record payment: " + e.getMessage()
                    ));
        }
    }

    // GET DEBTOR BY PHONE
    @GetMapping("/debtors/{phone}")
    public ResponseEntity<?> getDebtorByPhone(@PathVariable String phone) {
        try {
            List<Sale> customerSales = saleRepository.findActiveByCustomerPhoneAndBalanceDueGreaterThan(phone, 0.0);

            if (customerSales.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "success", false,
                                "message", "No active debts found for this customer"
                        ));
            }

            DebtorDTO debtor = DebtorDTO.builder()
                    .id(1L)
                    .customerName(customerSales.get(0).getCustomerName())
                    .customerPhone(phone)
                    .totalDebt(customerSales.stream().mapToDouble(Sale::getBalanceDue).sum())
                    .lastSaleDate(String.valueOf(customerSales.get(0).getSaleDate()))
                    .paymentStatus(calculatePaymentStatus(String.valueOf(customerSales.get(0).getSaleDate())))
                    .sales(customerSales.stream().map(this::convertToSaleDTO).collect(Collectors.toList()))
                    .build();

            return ResponseEntity.ok(debtor);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "Failed to fetch debtor: " + e.getMessage()
                    ));
        }
    }

    // RETURN ITEMS
    @PostMapping("/{saleId}/return")
    public ResponseEntity<?> returnSaleItems(
            @PathVariable Long saleId,
            @RequestBody ReturnRequest request) {

        try {
            Optional<Sale> saleOpt = saleRepository.findActiveById(saleId);

            if (saleOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "success", false,
                                "message", "Sale not found or has been deleted"
                        ));
            }

            saleService.processReturn(saleId, request.getItems());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Return processed successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "Failed to process return: " + e.getMessage()
                    ));
        }
    }

    // HELPER METHODS
    private SaleDTO convertToSaleDTO(Sale sale) {
        return SaleDTO.builder()
                .id(sale.getId())
                .customerPhone(sale.getCustomerPhone())
                .customerName(sale.getCustomerName())
                .saleDate(String.valueOf(sale.getSaleDate()))
                .totalAmount(sale.getTotalAmount())
                .paidAmount(sale.getPaidAmount())
                .changeAmount(sale.getChangeAmount())
                .balanceDue(sale.getBalanceDue())
                .paymentMethod(sale.getPaymentMethod())
                .profit(sale.getProfit())
                .paymentStatus(sale.getPaymentStatus())
                .isDeleted(sale.getIsDeleted())
                .deletedAt(sale.getDeletedAt())
                .items(sale.getItems().stream().map(this::convertToSaleItemDTO).collect(Collectors.toList()))
                .build();
    }

    private SaleItemDTOs convertToSaleItemDTO(SaleItem item) {
        return SaleItemDTOs.builder()
                .id(item.getId())
                .productName(item.getProduct().getItemName())
                .quantity(item.getQuantity())
                .unitPrice(item.getProduct().getSellingPrice())
                .totalPrice(item.getTotal())
                .build();
    }

    private String calculatePaymentStatus(String saleDate) {
        try {
            LocalDate saleLocalDate = LocalDate.parse(saleDate);
            LocalDate today = LocalDate.now();

            if (saleLocalDate.plusDays(30).isBefore(today)) {
                return "OVERDUE";
            } else {
                return "PENDING";
            }
        } catch (Exception e) {
            return "PENDING";
        }
    }

    private boolean isMoreRecent(String date1, String date2) {
        try {
            LocalDate localDate1 = LocalDate.parse(date1);
            LocalDate localDate2 = LocalDate.parse(date2);
            return localDate1.isAfter(localDate2);
        } catch (Exception e) {
            return false;
        }
    }
}