package com.example.munyaka.controllers;

import com.example.munyaka.services.ExpenditureService;
import com.example.munyaka.tables.Expenditure;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/expenditures")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class ExpenditureController {

    private final ExpenditureService expenditureService;

    @GetMapping
    public ResponseEntity<Page<Expenditure>> getExpenditures(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search
    ) {
        Page<Expenditure> expenditures = expenditureService.getExpenditures(page, size, category, search);
        return ResponseEntity.ok(expenditures);
    }

    @PostMapping
    public ResponseEntity<Expenditure> createExpenditure(@RequestBody Expenditure expenditure) {
        return ResponseEntity.ok(expenditureService.save(expenditure));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Expenditure> updateExpenditure(@PathVariable Long id, @RequestBody Expenditure expenditure) {
        expenditure.setId(id);
        return ResponseEntity.ok(expenditureService.save(expenditure));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpenditure(@PathVariable Long id) {
        expenditureService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
