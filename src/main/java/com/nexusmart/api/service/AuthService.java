package com.nexusmart.api.service;

import com.nexusmart.api.dto.request.LoginRequest;
import com.nexusmart.api.dto.request.RegisterRequest;
import com.nexusmart.api.dto.response.AuthResponse;
import com.nexusmart.api.entity.User;
import com.nexusmart.api.exception.AuthenticationException;
import com.nexusmart.api.repository.UserRepository;
import com.nexusmart.api.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, JwtTokenProvider jwtTokenProvider, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthenticationException("Invalid credentials"));
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new AuthenticationException("Invalid credentials");
        }
        
        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole());
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
}
