package com.example.munyaka.tables;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "suppliers")
@Data // Lombok: getters, setters, toString, equals/hashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Supplier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String phone;

    private String address;
}

