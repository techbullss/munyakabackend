package com.example.munyaka.controllers;
import com.example.munyaka.services.EmployeeService;
import com.example.munyaka.tables.Employee;
import com.example.munyaka.tables.EmployeeDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")   // for Next.js dev
public class EmployeeController {

    private final EmployeeService service;

    @GetMapping
    public Page<EmployeeDTO> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String department,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Employee> employees = service.list(search, department, pageable);

        return employees.map(e -> {
            long periods = service.getDuePeriods(e);
            boolean due = periods > 0;
            double totalDue = e.getSalary() * periods;
            return new EmployeeDTO(e, due, periods, totalDue);
        });
    }

    @PostMapping("/{id}/pay")
    public EmployeeDTO paySalary(@PathVariable Long id) {
        return service.paySalary(id);
    }
    @GetMapping("/{id}")
    public Employee get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    public Employee create(@RequestBody Employee employee) {
        employee.setId(null);
        return service.save(employee);
    }

    @PutMapping("/{id}")
    public Employee update(@PathVariable Long id, @RequestBody Employee employee) {
        employee.setId(id);
        return service.save(employee);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
