package com.example.munyaka.services;

import com.example.munyaka.repository.EmployeeRepository;
import com.example.munyaka.tables.Employee;
import com.example.munyaka.tables.EmployeeDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository repo;

    public Page<Employee> list(String search, String department, Pageable pageable) {
        String s = (search == null || search.isBlank()) ? null : search;
        String d = (department == null || department.isBlank()) ? null : department;
        return repo.search(d, s, pageable);
    }
    public EmployeeDTO paySalary(Long id) {
        Employee employee = get(id);

        // Calculate how many salary periods are due
        long duePeriods = getDuePeriods(employee);

        // Optionally: calculate total due amount
        double totalDue = employee.getSalary() * duePeriods;

        // Update lastPaidDate to today
        employee.setLastPaidDate(LocalDate.now());
        employee = save(employee);

        // Check if employee is still due (should be false after payment)
        boolean due = isSalaryDue(employee);

        // Return DTO with due periods and optional totalDue
        return new EmployeeDTO(employee, due, duePeriods, totalDue);
    }

    /**
     * Calculates how many salary periods are due based on lastPaidDate and salaryType.
     */
    public long getDuePeriods(Employee e) {
        if (e.getLastPaidDate() == null) return 1; // never paid, assume 1 period

        long daysSinceLast = ChronoUnit.DAYS.between(e.getLastPaidDate(), LocalDate.now());

        return switch (e.getSalaryType()) {
            case Daily -> daysSinceLast;
            case Weekly -> daysSinceLast / 7;
            case Monthly -> daysSinceLast / 30;
        };
    }

    /**
     * Checks if the employee is due for payment (true if at least 1 period is missed)
     */
    public boolean isSalaryDue(Employee e) {
        return getDuePeriods(e) > 0;
    }


    public Employee get(Long id) {
        return repo.findById(id).orElseThrow();
    }

    public Employee save(Employee e) {
        return repo.save(e);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }
}
