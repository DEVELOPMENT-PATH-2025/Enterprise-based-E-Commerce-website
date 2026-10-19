package com.nexusmart.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/")
    public Map<String, Object> home() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Welcome to NexusMart API");
        response.put("version", "1.0.0");
        response.put("endpoints", Map.of(
            "auth", "/api/auth/**",
            "products", "/api/products/**",
            "orders", "/api/orders/**",
            "h2-console", "/h2-console",
            "actuator", "/actuator"
        ));
        return response;
    }
}
