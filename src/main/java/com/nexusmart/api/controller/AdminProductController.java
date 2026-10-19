package com.nexusmart.api.controller;

import com.nexusmart.api.dto.request.ProductCreateRequest;
import com.nexusmart.api.dto.request.ProductUpdateRequest;
import com.nexusmart.api.dto.response.AdminStatsResponse;
import com.nexusmart.api.dto.response.ProductResponse;
import com.nexusmart.api.entity.Product;
import com.nexusmart.api.repository.OrderRepository;
import com.nexusmart.api.repository.ProductRepository;
import com.nexusmart.api.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin/products")
@PreAuthorize("hasRole('ADMIN')")
public class AdminProductController {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public AdminProductController(ProductRepository productRepository, OrderRepository orderRepository) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductCreateRequest request) {
        Product product = new Product(
            request.getName(),
            request.getDescription(),
            request.getPrice(),
            request.getStock(),
            request.getImageUrl(),
            request.getCategory()
        );
        
        Product savedProduct = productRepository.save(product);
        return ResponseEntity.ok(mapToResponse(savedProduct));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable UUID id,
            @Valid @RequestBody ProductUpdateRequest request) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Product not found"));

        if (request.getName() != null && !request.getName().equals(product.getName())) {
            product.setName(request.getName());
        }

        if (request.getDescription() != null && !request.getDescription().equals(product.getDescription())) {
            product.setDescription(request.getDescription());
        }

        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }

        if (request.getStock() != null) {
            product.setStock(request.getStock());
        }

        if (request.getCategory() != null) {
            product.setCategory(request.getCategory());
        }

        if (request.getImageUrl() != null) {
            product.setImageUrl(request.getImageUrl());
        }


        Product savedProduct = productRepository.save(product);
        return ResponseEntity.ok(mapToResponse(savedProduct));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable UUID id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Product not found");
        }
        productRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats")
    public ResponseEntity<AdminStatsResponse> getAdminStats() {
        BigDecimal totalRevenue = orderRepository.findAll().stream()
            .filter(order -> "PAID".equals(order.getStatus()))
            .map(order -> order.getTotalAmount())
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        Long totalOrders = orderRepository.count();
        Long activeSkus = productRepository.count();
        Long lowStockItems = productRepository.findByStockLessThan(5).stream().count();

        AdminStatsResponse stats = new AdminStatsResponse(totalRevenue, totalOrders, activeSkus, lowStockItems);
        return ResponseEntity.ok(stats);
    }

    private ProductResponse mapToResponse(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setPrice(product.getPrice());
        response.setStock(product.getStock());
        response.setImageUrl(product.getImageUrl());
        response.setCategory(product.getCategory());
        response.setCreatedAt(product.getCreatedAt());
        response.setUpdatedAt(product.getUpdatedAt());
        return response;
    }
}
