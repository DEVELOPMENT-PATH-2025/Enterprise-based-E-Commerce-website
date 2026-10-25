package com.nexusmart.api.service;

import com.nexusmart.api.dto.request.ProductCreateRequest;
import com.nexusmart.api.dto.request.ProductUpdateRequest;
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

    public Product createProduct(ProductCreateRequest request) {
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setImageUrl(request.getImageUrl());
        product.setCategory(request.getCategory());
        return productRepository.save(product);
    }

    public void updateProduct(UUID id, ProductUpdateRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        
        if (request.getName() != null) {
            product.setName(request.getName());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }
        if (request.getStock() != null) {
            product.setStock(request.getStock());
        }
        if (request.getImageUrl() != null) {
            product.setImageUrl(request.getImageUrl());
        }
        if (request.getCategory() != null) {
            product.setCategory(request.getCategory());
        }
        
        productRepository.save(product);
    }

    public void deleteProduct(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        productRepository.delete(product);
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
        
        // Include vendor name if product has a vendor
        if (product.getVendor() != null) {
            response.setVendorName(product.getVendor().getStoreName());
        }
        
        return response;
    }
}
