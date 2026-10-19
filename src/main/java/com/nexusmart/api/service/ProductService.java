package com.nexusmart.api.service;

import com.nexusmart.api.dto.request.SearchRequest;
import com.nexusmart.api.dto.response.ProductResponse;
import com.nexusmart.api.entity.Product;
import com.nexusmart.api.exception.ResourceNotFoundException;
import com.nexusmart.api.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ProductResponse getProductById(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        return mapToResponse(product);
    }

    public List<ProductResponse> searchProducts(SearchRequest request) {
        // TODO: Implement semantic search using OpenAI embeddings + PGVector
        int limit = request.getLimit() != null ? request.getLimit() : 10;
        return productRepository.findAll().stream()
                .limit(limit)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private ProductResponse mapToResponse(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setPrice(product.getPrice());
        response.setImageUrl(product.getImageUrl());
        response.setCategory(product.getCategory());
        return response;
    }
}
