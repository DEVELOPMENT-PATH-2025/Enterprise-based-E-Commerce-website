package com.nexusmart.api.controller;

import com.nexusmart.api.dto.request.LoginRequest;
import com.nexusmart.api.dto.request.VendorRegisterRequest;
import com.nexusmart.api.dto.response.AuthResponse;
import com.nexusmart.api.entity.User;
import com.nexusmart.api.entity.Vendor;
import com.nexusmart.api.entity.VendorStatus;
import com.nexusmart.api.repository.UserRepository;
import com.nexusmart.api.repository.VendorRepository;
import com.nexusmart.api.security.JwtTokenProvider;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/vendor/auth")
@CrossOrigin(origins = "*")
public class VendorAuthController {

    private final UserRepository userRepository;
    private final VendorRepository vendorRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public VendorAuthController(
            UserRepository userRepository,
            VendorRepository vendorRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.vendorRepository = vendorRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerVendor(@Valid @RequestBody VendorRegisterRequest request) {
        // Check if email is already registered
        Optional<User> existingUser = userRepository.findByEmail(request.getBusinessEmail().toLowerCase().trim());
        if (existingUser.isPresent()) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Email is already registered");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }

        // Check if store name is already registered
        Optional<Vendor> existingVendor = vendorRepository.findByStoreSlug(request.getStoreSlug().trim().toLowerCase());
        if (existingVendor.isPresent()) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Store slug is already taken");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }

        // Create new User with ROLE_VENDOR
        User user = new User();
        user.setEmail(request.getBusinessEmail().toLowerCase().trim());
        user.setName(request.getStoreName().trim());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole("ROLE_VENDOR");
        User savedUser = userRepository.save(user);

        // Create associated Vendor record
        Vendor vendor = new Vendor(
                savedUser,
                request.getStoreName().trim(),
                request.getStoreSlug().trim().toLowerCase(),
                request.getBusinessEmail().trim()
        );
        vendor.setStatus(VendorStatus.ACTIVE);
        vendor.setCommissionRate(new BigDecimal("10.00"));
        Vendor savedVendor = vendorRepository.save(vendor);

        // Generate JWT token
        String token = jwtTokenProvider.generateToken(savedUser.getId(), savedUser.getEmail(), savedUser.getRole());

        // Build response
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", savedUser.getId());
        userMap.put("name", savedUser.getName());
        userMap.put("email", savedUser.getEmail());
        userMap.put("role", savedUser.getRole());

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("user", userMap);
        response.put("storeName", savedVendor.getStoreName());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginVendor(@Valid @RequestBody LoginRequest request) {
        // Find user by email
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail().toLowerCase().trim());
        if (userOpt.isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Invalid email or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        User user = userOpt.get();

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Invalid email or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        // Verify user has ROLE_VENDOR
        if (!"ROLE_VENDOR".equals(user.getRole())) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "This account is not registered as a seller. Please sign in through the customer portal.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }

        // Generate JWT token
        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole());

        // Build response
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", user.getId());
        userMap.put("name", user.getName());
        userMap.put("email", user.getEmail());
        userMap.put("role", user.getRole());

        // Get vendor store name
        Optional<Vendor> vendorOpt = vendorRepository.findByUserId(user.getId());
        String storeName = vendorOpt.map(Vendor::getStoreName).orElse(null);

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("user", userMap);
        if (storeName != null) {
            response.put("storeName", storeName);
        }

        return ResponseEntity.ok(response);
    }
}
