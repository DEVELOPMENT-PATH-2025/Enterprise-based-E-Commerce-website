package com.nexusmart.api.service;

import com.nexusmart.api.dto.request.LoginRequest;
import com.nexusmart.api.dto.request.ProfileUpdateRequest;
import com.nexusmart.api.dto.request.RegisterRequest;
import com.nexusmart.api.dto.response.AuthResponse;
import com.nexusmart.api.entity.User;
import com.nexusmart.api.entity.Vendor;
import com.nexusmart.api.exception.AuthenticationException;
import com.nexusmart.api.repository.UserRepository;
import com.nexusmart.api.repository.VendorRepository;
import com.nexusmart.api.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final VendorRepository vendorRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, VendorRepository vendorRepository, JwtTokenProvider jwtTokenProvider, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.vendorRepository = vendorRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthenticationException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new AuthenticationException("Invalid credentials");
        }

        UUID vendorId = null;
        if ("ROLE_VENDOR".equals(user.getRole())) {
            vendorId = vendorRepository.findByUserId(user.getId())
                    .map(Vendor::getId)
                    .orElse(null);
        }

        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole(), vendorId);
        return new AuthResponse(token, user.getId(), user.getEmail(), user.getName(), user.getRole());
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AuthenticationException("Email already registered");
        }

        String role = request.getRole() != null ? request.getRole() : "ROLE_CUSTOMER";

        User user = new User(
            request.getEmail(),
            passwordEncoder.encode(request.getPassword()),
            request.getName(),
            role
        );
        userRepository.save(user);

        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole());
        return new AuthResponse(token, user.getId(), user.getEmail(), user.getName(), user.getRole());
    }

    public AuthResponse updateProfile(ProfileUpdateRequest request, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthenticationException("User not found"));

        // Update name
        user.setName(request.getName());

        // Update password if provided
        if (request.getNewPassword() != null && !request.getNewPassword().isEmpty()) {
            if (request.getCurrentPassword() == null || request.getCurrentPassword().isEmpty()) {
                throw new AuthenticationException("Current password is required to change password");
            }
            if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
                throw new AuthenticationException("Current password is incorrect");
            }
            user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        }

        userRepository.saveAndFlush(user);

        // Generate new token with updated info
        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole());
        return new AuthResponse(token, user.getId(), user.getEmail(), user.getName(), user.getRole());
    }
}
