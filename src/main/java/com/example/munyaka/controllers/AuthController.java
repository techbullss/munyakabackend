package com.example.munyaka.controllers;
import com.example.munyaka.DTO.LoginRequest;
import com.example.munyaka.DTO.LoginResponse;
import com.example.munyaka.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        try {
            LoginResponse response = userService.authenticateUser(
                    loginRequest.getEmail(),
                    loginRequest.getPassword()
            );

            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new LoginResponse(false, "Login failed: " + e.getMessage())
            );
        }
    }
}