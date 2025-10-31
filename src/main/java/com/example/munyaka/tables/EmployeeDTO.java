package com.example.munyaka.tables;

public record EmployeeDTO(
        Employee employee,
        boolean due,
        long duePeriods,       // number of unpaid periods (days/weeks/months)
        double totalDue        // total salary due for missed periods
) {}
