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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    @PostMapping
    public SaleReceiptResponse createSale(@RequestBody SaleRequest request) {
        Sale sale = new Sale();
        sale.setCustomerName(request.getCustomerName());
        sale.setSaleDate(LocalDate.parse(LocalDate.now().toString()));
        sale.setTotalAmount(request.getTotalAmount());
        sale.setBalanceDue(request.getChangeAmount());
        sale.setPaidAmount(request.getAmountPaid());
        sale.setPaymentMethod(request.getPaymentMethod());
        sale.setCustomerPhone(request.getCustomerPhone());

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
            saleItems.add(saleItem);

            total += product.getSellingPrice() * reqItem.getQuantity();

            // ✅ Profit calculation per item
            double profit = (product.getSellingPrice() - product.getPrice() - reqItem.getDiscountAmount())
                    * reqItem.getQuantity();
            totalProfit += profit;

            // Build receipt item
            ReceiptItem dto = new ReceiptItem();
            dto.setProductName(product.getItemName());
            dto.setQuantity(reqItem.getQuantity());
            dto.setUnitPrice(product.getSellingPrice());
            dto.setLineTotal(product.getSellingPrice() * reqItem.getQuantity());
            dto.setProfit(profit);
            receiptItems.add(dto);
        }

        sale.setItems(saleItems);
        sale.setProfit(totalProfit);

        //  Determine payment status
        double totalAmount = request.getTotalAmount();
        double paidAmount = request.getAmountPaid();


// Status logic
        if (paidAmount == 0) {
            sale.setPaymentStatus("PENDING");   // no payment made
        } else if (paidAmount < totalAmount) {
            sale.setPaymentStatus("PARTIAL");   // some paid, but not full
        } else if (paidAmount == totalAmount) {
            sale.setPaymentStatus("PAID");      // fully paid
        } else {
            sale.setPaymentStatus("OVERPAID");  // paid more than required
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

    @PutMapping("/{id}")
    public ResponseEntity<?> updateSale(
            @PathVariable Long id,
            @RequestBody Sale updatedSale
    ) {
        return saleRepository.findById(id).map(existingSale -> {
            // update fields
            existingSale.setCustomerName(updatedSale.getCustomerName());
            existingSale.setCustomerPhone(updatedSale.getCustomerPhone());
            existingSale.setSaleDate(updatedSale.getSaleDate());
            existingSale.setPaymentMethod(updatedSale.getPaymentMethod());
            existingSale.setPaidAmount(updatedSale.getPaidAmount());

            // recalc totals
            double totalAmount = existingSale.getItems().stream()
                    .mapToDouble(SaleItem::getTotal)
                    .sum();

            double balance = totalAmount - updatedSale.getPaidAmount();
            existingSale.setTotalAmount(totalAmount);
            existingSale.setBalanceDue(balance);

            if (updatedSale.getPaidAmount() >= totalAmount) {
                existingSale.setPaymentStatus(updatedSale.getPaidAmount() > totalAmount ? "OVERPAID" : "PAID");
            } else if (updatedSale.getPaidAmount() > 0) {
                existingSale.setPaymentStatus("PARTIAL");
            } else {
                existingSale.setPaymentStatus("PENDING");
            }

            Sale savedSale = saleRepository.save(existingSale);
            Map<String, Object> response = new HashMap<>();
            response.put("id", savedSale.getId());
            response.put("status", savedSale.getPaymentStatus());
            response.put("totalAmount", savedSale.getTotalAmount());
            response.put("balanceDue", savedSale.getBalanceDue());
            // ✅ No JSON returned, just 204
            return ResponseEntity.ok(response);
        }).orElse(ResponseEntity.notFound().build()); // <- fix
    }



    @GetMapping("/{id}")
    public SaleSummaryDTO getSale(@PathVariable Long id) {
        Sale sale = repo.findById(id).orElseThrow();
        return new SaleSummaryDTO(sale);
    }
    @GetMapping("/filter")
    public Map<String, Object> getSalesByPeriod(
            @RequestParam String start,
            @RequestParam String end,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "saleDate,desc") String sort) {

        LocalDate startDate = LocalDate.parse(start );
        String endDate = end ;

        // Parse sort param
        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        Sort.Direction direction = (sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc"))
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

        // Fetch paginated sales (for table display)
        Page<Sale> salesPage = saleRepository.findBySaleDateBetween(startDate, LocalDate.parse(endDate), pageable);
        List<SaleSummaryDTO> saleDTOs = salesPage.getContent()
                .stream()
                .map(SaleSummaryDTO::new)  // ✅ DTO must copy sale.getProfit()
                .toList();

        // Fetch all sales (for summary calculations, not paginated)
        List<Sale> allSales = saleRepository.findBySaleDateBetween(startDate, LocalDate.parse(endDate));
        List<SaleSummaryDTO> allSaleDTOs = allSales.stream()
                .map(SaleSummaryDTO::new)
                .toList();

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
                .mapToDouble(SaleSummaryDTO::getProfit)  // ✅ add this
                .sum();

        // Previous period deviation
        long daysBetween = java.time.Duration.between(
                java.time.LocalDate.parse(start).atStartOfDay(),
                java.time.LocalDate.parse(end).atStartOfDay()
        ).toDays() + 1;

        LocalDate prevStart = startDate.minusDays(daysBetween);
        String prevEnd = String.valueOf(LocalDate.parse(start).minusDays(1));

        double prevAmount = saleRepository.findBySaleDateBetween(prevStart, LocalDate.parse(prevEnd))
                .stream()
                .mapToDouble(s -> s.getTotalAmount() != null ? s.getTotalAmount() : 0.0)
                .sum();

        double deviation = prevAmount == 0
                ? 100
                : ((totalAmount - prevAmount) / prevAmount) * 100;

        // Build response
        Map<String, Object> response = new HashMap<>();
        response.put("sales", saleDTOs); //  only current page
        response.put("summary", Map.of(
                "totalSales", totalSales,
                "totalAmount", totalAmount,
                "totalProfit", totalProfit,   //  include profit in summary
                "averageSale", avgSale,
                "totalItems", totalItems,
                "deviation", deviation
        ));
        response.put("page", salesPage.getNumber());
        response.put("size", salesPage.getSize());
        response.put("totalElements", salesPage.getTotalElements());
        response.put("totalPages", salesPage.getTotalPages());

        return response;
    }


    @GetMapping
    public Map<String, Object> getSalesPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "saleDate") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Sale> salePage = saleRepository.findAll(pageable);


        double totalProfit = salePage.getContent()
                .stream()
                .mapToDouble(Sale::getProfit)
                .sum();

        List<SaleSummaryDTO> sales = salePage.getContent()
                .stream()
                .map(SaleSummaryDTO::new)
                .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("sales", sales);
        response.put("currentPage", salePage.getNumber());
        response.put("totalItems", salePage.getTotalElements());
        response.put("totalPages", salePage.getTotalPages());
        response.put("totalProfit", totalProfit);

        return response;
    }
    @GetMapping("/debtors")
    public ResponseEntity<List<DebtorDTO>> getDebtors() {
        try {
            List<Sale> pendingSales = saleRepository.findByBalanceDueGreaterThan(0.0);
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
            return ResponseEntity.internalServerError().build();
        }
    }

    // Get pending sales
    @GetMapping("/pending")
    public ResponseEntity<List<SaleDTO>> getPendingSales() {
        try {
            List<Sale> pendingSales = saleRepository.findByBalanceDueGreaterThan(0.0);
            List<SaleDTO> saleDTOs = pendingSales.stream()
                    .map(this::convertToSaleDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(saleDTOs);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Record payment for a specific sale
    @PostMapping("/payment/{id}")
    public ResponseEntity<SaleDTO> recordPayment(
            @PathVariable Long id,
            @RequestBody PaymentRequest paymentRequest) {
        System.out.println("Received payment for sale ID: " + id +
                ", Amount: " + paymentRequest.getPaymentAmount());
        try {
            Optional<Sale> saleOpt = saleRepository.findById(id);
            if (saleOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Sale sale = saleOpt.get();
            Double paymentAmount = paymentRequest.getPaymentAmount();

            if (paymentAmount <= 0) {
                return ResponseEntity.badRequest().build();
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
            return ResponseEntity.internalServerError().build();
        }
    }

    // Get debtor by phone number
    @GetMapping("/debtors/{phone}")
    public ResponseEntity<DebtorDTO> getDebtorByPhone(@PathVariable String phone) {
        try {
            List<Sale> customerSales = saleRepository.findByCustomerPhoneAndBalanceDueGreaterThan(phone, 0.0);

            if (customerSales.isEmpty()) {
                return ResponseEntity.notFound().build();
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
            return ResponseEntity.internalServerError().build();
        }
    }

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
    @PostMapping("/{saleId}/return")
    public ResponseEntity<?> returnSaleItems(
            @PathVariable Long saleId,
            @RequestBody ReturnRequest request) {

        saleService.processReturn(saleId, request.getItems());
        return ResponseEntity.ok("Return processed successfully");
    }
}
