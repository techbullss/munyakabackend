package com.example.munyaka.repository;

import com.example.munyaka.tables.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    @Query("""
           SELECT e FROM Employee e
           WHERE (:department IS NULL OR e.department = :department)
             AND (:search IS NULL OR 
                 LOWER(e.name) LIKE LOWER(CONCAT('%',:search,'%')) OR
                 LOWER(e.email) LIKE LOWER(CONCAT('%',:search,'%')) OR
                 LOWER(e.phone) LIKE LOWER(CONCAT('%',:search,'%'))
           )
           """)
    Page<Employee> search(
            @Param("department") String department,
            @Param("search") String search,
            Pageable pageable
    );
}
