package com.nexusmart.api.controller;

import com.nexusmart.api.dto.request.LoginRequest;
import com.nexusmart.api.dto.request.RegisterRequest;
import com.nexusmart.api.dto.response.AuthResponse;
import com.nexusmart.api.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/google")
    public ResponseEntity<AuthResponse> googleAuth(@RequestBody String googleToken) {
        return ResponseEntity.ok().build();
    }
}
