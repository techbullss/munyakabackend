package com.example.munyaka.services;
import com.example.munyaka.DTO.PurchaseItemDto;
import com.example.munyaka.DTO.PurchaseResponse;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import com.example.munyaka.DTO.PurchaseRequest;
import com.example.munyaka.repository.PurchaseRepository;
import com.example.munyaka.tables.Purchase;
import com.example.munyaka.tables.PurchaseItem;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
@Service
@RequiredArgsConstructor
public class PurchaseService {

    private final PurchaseRepository purchaseRepo;
    private PurchaseResponse toDto(Purchase p) {
        List<PurchaseItemDto> itemDtos = p.getItems().stream()
                .map(i -> new PurchaseItemDto(
                        i.getId(),
                        i.getProductName(),
                        i.getPrice(),
                        i.getQuantity(),
                        i.getTotal()
                ))
                .toList();

        return new PurchaseResponse(
                p.getId(),
                p.getSupplierName(),
                p.getSupplierEmail(),      // Position 3: supplierEmail
                p.getSupplierPhone(),      // Position 4: supplierPhone
                p.getPurchaseDate(),       // Position 5: purchaseDate
                p.getTotalAmount(),
                p.getAmountPaid(),
                p.getBalanceDue(),
                p.getStatus(),
                p.isCreditor(),
                itemDtos
        );
    }
    @Transactional
    public PurchaseResponse create(PurchaseRequest dto) {
        Purchase p = Purchase.builder()
                .supplierName(dto.supplierName())
                .supplierEmail(dto.supplierEmail())
                .supplierPhone(dto.supplierPhone())
                .purchaseDate(dto.purchaseDate())
                .totalAmount(dto.totalAmount())
                .amountPaid(dto.amountPaid())
                .balanceDue(dto.balanceDue())
                .status(dto.status())
                .creditor(dto.creditor())
                .build();

        List<PurchaseItem> items = dto.items().stream()
                .map(i -> PurchaseItem.builder()
                        .productName(i.productName())
                        .price(i.price())
                        .quantity(i.quantity())
                        .total(i.total())
                        .purchase(p)
                        .build())
                .toList();

        p.setItems(items);
        Purchase saved = purchaseRepo.save(p);
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public Page<PurchaseResponse> filter(
            String supplier,
            String status,
            LocalDate start,
            LocalDate end,
            Pageable pageable
    ) {
        Specification<Purchase> spec = Specification.where(null);

        if (supplier != null && !supplier.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("supplierName")), "%" + supplier.toLowerCase() + "%")
            );
        }

        if (status != null && !status.equalsIgnoreCase("All")) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("status"), status)
            );
        }

        if (start != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("purchaseDate"), start)
            );
        }

        if (end != null) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("purchaseDate"), end)
            );
        }

        return purchaseRepo.findAll(spec, pageable)
                .map(this::toDto);
    }

    @Transactional
    public PurchaseResponse update(Long id, PurchaseRequest dto) {
        Purchase p = purchaseRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Purchase not found"));

        p.setSupplierName(dto.supplierName());
        p.setPurchaseDate(dto.purchaseDate());
        p.setTotalAmount(dto.totalAmount());
        p.setAmountPaid(dto.amountPaid());
        p.setBalanceDue(dto.balanceDue());
        p.setStatus(dto.status());
        p.setCreditor(dto.creditor());

        // replace items
        p.getItems().clear();
        List<PurchaseItem> items = dto.items().stream()
                .map(i -> PurchaseItem.builder()
                        .productName(i.productName())
                        .price(i.price())
                        .quantity(i.quantity())
                        .total(i.total())
                        .purchase(p)
                        .build())
                .toList();
        p.getItems().addAll(items);

        Purchase saved = purchaseRepo.save(p); // <-- important
        return toDto(saved);
    }

    @Transactional
    public void delete(Long id) {
        purchaseRepo.deleteById(id);
    }
}

