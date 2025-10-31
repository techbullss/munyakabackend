package com.example.munyaka.tables;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "employees")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    private String phone;
    private String position;
    private String department;

    private Double salary;
    private LocalDate lastPaidDate;

    @Enumerated(EnumType.STRING)
    private SalaryType salaryType;

    @Enumerated(EnumType.STRING)
    private Status status;

    public enum SalaryType { Daily, Weekly, Monthly }
    public enum Status { Active, On_Leave }  // Maps to "Active"/"On Leave" via JSON
}
