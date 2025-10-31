package com.example.munyaka.DTO;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
@Data
@Setter
@Getter
public class UserDTO {
    private Long id;
    private String name;
    private String email;
    private String role;
    private String status;
    private String phone;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;

    // Constructors, Getters, Setters
}
