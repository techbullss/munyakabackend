package com.example.munyaka.services;

import com.example.munyaka.repository.ExpenditureRepository;
import com.example.munyaka.tables.Expenditure;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ExpenditureService {

    private final ExpenditureRepository repository;

    public Page<Expenditure> getExpenditures(int page, int size, String category, String search) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending()); // or whatever field you want to sort by

        // Remove all date filtering logic since there's no date field
        if ((category == null || category.isEmpty()) && (search == null || search.isEmpty())) {
            return repository.findAll(pageable);
        } else if (category != null && !category.isEmpty() && (search == null || search.isEmpty())) {
            return repository.findByCategoryContainingIgnoreCase(category, pageable);
        } else if ((category == null || category.isEmpty()) && search != null && !search.isEmpty()) {
            return repository.findByDescriptionContainingIgnoreCase(search, pageable);
        } else {
            return repository.findByCategoryContainingIgnoreCaseAndDescriptionContainingIgnoreCase(category, search, pageable);
        }
    }
    public Expenditure save(Expenditure expenditure) {
        return repository.save(expenditure);
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}

