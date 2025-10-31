package com.example.munyaka.controllers;

import com.example.munyaka.DTO.PurchaseRequest;
import com.example.munyaka.DTO.PurchaseResponse;
import com.example.munyaka.services.PurchaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/purchases")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class PurchaseController {

    private final PurchaseService service;

    @PostMapping
    public PurchaseResponse create(@RequestBody PurchaseRequest dto) {
        return service.create(dto);
    }

    @GetMapping("/filter")
    public Page<PurchaseResponse> filter(
            @RequestParam(required = false) String supplier,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return service.filter(supplier, status, start, end,
                PageRequest.of(page, size, Sort.by("purchaseDate").descending()));
    }

    @PutMapping("/{id}")
    public PurchaseResponse update(@PathVariable Long id, @RequestBody PurchaseRequest dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
