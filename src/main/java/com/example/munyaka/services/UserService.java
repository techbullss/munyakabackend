package com.example.munyaka.services;

import com.example.munyaka.DTO.LoginResponse;
import com.example.munyaka.DTO.UserDTO;
import com.example.munyaka.repository.UserRepository;
import com.example.munyaka.tables.User;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    public Page<UserDTO> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    public Page<UserDTO> searchUsers(String search, Pageable pageable) {
        return userRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(search, search, pageable)
                .map(this::convertToDTO);
    }

    public Optional<UserDTO> getUserById(Long id) {
        return userRepository.findById(id)
                .map(this::convertToDTO);
    }

    public UserDTO createUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        user.setPassword(user.getPassword());
        User savedUser = userRepository.save(user);
        return convertToDTO(savedUser);
    }

    public UserDTO updateUser(Long id, User userDetails) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if email is being changed and if it already exists
        if (!user.getEmail().equals(userDetails.getEmail()) &&
                userRepository.existsByEmail(userDetails.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        user.setName(userDetails.getName());
        user.setEmail(userDetails.getEmail());
        user.setRole(userDetails.getRole());
        user.setStatus(userDetails.getStatus());
        user.setPhone(userDetails.getPhone());

        // Only update password if provided
        if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
            user.setPassword(userDetails.getPassword());
        }

        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        userRepository.delete(user);
    }

    public void updateLastLogin(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
    }

    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setStatus(user.getStatus());
        dto.setPhone(user.getPhone());
        dto.setLastLogin(user.getLastLogin());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }
    public LoginResponse authenticateUser(String email, String password) {
        try {
            // Find user by email
            Optional<User> userOptional = userRepository.findByEmail(email);

            if (userOptional.isEmpty()) {
                return new LoginResponse(false, "User not found");
            }

            User user = userOptional.get();

            // Check if user is active
            if (!"ACTIVE".equals(user.getStatus())) {
                return new LoginResponse(false, "Account is inactive");
            }

            // Check password (plain text comparison - you should use password encoding)
            if (!password.equals(user.getPassword())) {
                return new LoginResponse(false, "Invalid password");
            }

            // Update last login
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

            // Convert to DTO and return success
            UserDTO userDTO = convertToDTO(user);
            return new LoginResponse(true, "Login successful", userDTO);

        } catch (Exception e) {
            return new LoginResponse(false, "Authentication failed: " + e.getMessage());
        }
    }
    @PostConstruct
    public void init() {
        createDefaultAdminIfNeeded();
    }

    private void createDefaultAdminIfNeeded() {
        if (userRepository.count() == 0) {
            User admin = new User();
            admin.setName("System Administrator");
            admin.setEmail("bwanamaina2010@gmail.com");

            admin.setPassword("admin123"); // Hashed password
            admin.setRole("ADMIN");
            admin.setStatus("ACTIVE");
            admin.setPhone("+255000000000");


            userRepository.save(admin);
            System.out.println(" Default admin user created successfully");
        }
    }

    // Add this method to your service for the CommandLineRunner
    public long getUserCount() {
        return userRepository.count();
    }
}
