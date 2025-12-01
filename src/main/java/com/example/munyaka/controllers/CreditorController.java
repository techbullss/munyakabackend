package com.example.munyaka.controllers;

import com.example.munyaka.DTO.CreditorResponse;
import com.example.munyaka.services.CreditorService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/creditors")
@RequiredArgsConstructor
public class CreditorController {

    private final CreditorService creditorService;

    @GetMapping
    public Page<CreditorResponse> getCreditors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return creditorService.listCreditors(PageRequest.of(page, size));
    }
    @PutMapping("/pay/{id}")
    public CreditorResponse recordPayment(
            @PathVariable Long id,
            @RequestBody Map<String, Double> body // expects { "amount": 100.0 }
    ) {
        double amount = body.get("amount");
        return creditorService.recordPayment(id, amount);
    }
}
