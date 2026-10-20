package com.nexusmart.api.controller;

import com.nexusmart.api.dto.response.ProductResponse;
import com.nexusmart.api.entity.Product;
import com.nexusmart.api.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductRepository productRepository;

    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean inStockOnly) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products;
        
        if (category != null && !category.isEmpty()) {
            products = productRepository.findByCategory(category).stream()
                .skip((long) page * size)
                .limit(size)
                .collect(Collectors.collectingAndThen(Collectors.toList(), list -> new PageImpl<>(list, pageable, list.size())));
        } else {
            List<Product> allProducts = productRepository.findAll();
            if (inStockOnly != null && inStockOnly) {
                allProducts = allProducts.stream()
                    .filter(p -> p.getStock() > 0)
                    .collect(Collectors.toList());
            }
            products = new PageImpl<>(
                allProducts.stream()
                    .skip((long) page * size)
                    .limit(size)
                    .collect(Collectors.toList()),
                pageable,
                allProducts.size()
            );
        }
        
        Page<ProductResponse> response = products.map(this::mapToResponse);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable UUID id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Product not found"));
        return ResponseEntity.ok(mapToResponse(product));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductResponse>> searchProducts(@RequestParam String q) {
        // Simple text-based search without AI embeddings
        List<Product> products = productRepository.findAll().stream()
            .filter(p -> p.getName().toLowerCase().contains(q.toLowerCase()) ||
                       p.getDescription().toLowerCase().contains(q.toLowerCase()))
            .collect(Collectors.toList());
        
        List<ProductResponse> response = products.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
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
