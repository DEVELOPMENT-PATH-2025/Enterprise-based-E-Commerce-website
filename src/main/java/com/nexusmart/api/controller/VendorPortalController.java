package com.nexusmart.api.controller;

import com.nexusmart.api.dto.request.ProductCreateRequest;
import com.nexusmart.api.dto.request.ProductUpdateRequest;
import com.nexusmart.api.dto.request.VendorRegisterRequest;
import com.nexusmart.api.dto.response.AuthResponse;
import com.nexusmart.api.dto.response.ProductResponse;
import com.nexusmart.api.dto.response.VendorStatsResponse;
import com.nexusmart.api.entity.OrderItem;
import com.nexusmart.api.entity.Product;
import com.nexusmart.api.entity.User;
import com.nexusmart.api.entity.Vendor;
import com.nexusmart.api.exception.ResourceNotFoundException;
import com.nexusmart.api.repository.OrderItemRepository;
import com.nexusmart.api.repository.ProductRepository;
import com.nexusmart.api.repository.UserRepository;
import com.nexusmart.api.repository.VendorRepository;
import com.nexusmart.api.security.JwtTokenProvider;
import com.nexusmart.api.service.AuthService;
import com.nexusmart.api.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/vendor")
@PreAuthorize("hasRole('VENDOR')")
public class VendorPortalController {

    private final VendorRepository vendorRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthService authService;
    private final ProductService productService;

    public VendorPortalController(
            VendorRepository vendorRepository,
            UserRepository userRepository,
            ProductRepository productRepository,
            OrderItemRepository orderItemRepository,
            JwtTokenProvider jwtTokenProvider,
            AuthService authService,
            ProductService productService) {
        this.vendorRepository = vendorRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.orderItemRepository = orderItemRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authService = authService;
        this.productService = productService;
    }

    @PostMapping("/register")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AuthResponse> registerVendor(
            @Valid @RequestBody VendorRegisterRequest request,
            Principal principal) {
        String email = principal.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (vendorRepository.findByUserId(user.getId()).isPresent()) {
            throw new RuntimeException("User is already registered as a vendor");
        }

        Vendor vendor = new Vendor(user, request.getStoreName(), request.getStoreSlug(), request.getBusinessEmail());
        vendorRepository.save(vendor);

        // Upgrade user role to ROLE_VENDOR
        user.setRole("ROLE_VENDOR");
        userRepository.save(user);

        // Generate new token with vendor role and vendorId
        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole(), vendor.getId());
        return ResponseEntity.ok(new AuthResponse(token, user.getId(), user.getEmail(), user.getName(), user.getRole()));
    }

    @GetMapping("/stats")
    public ResponseEntity<VendorStatsResponse> getVendorStats(Principal principal) {
        String email = principal.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Vendor vendor = vendorRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Vendor profile not found"));

        // Calculate total net earnings from vendor payouts
        List<OrderItem> vendorOrderItems = orderItemRepository.findByVendorId(vendor.getId());
        BigDecimal totalNetEarnings = vendorOrderItems.stream()
                .filter(item -> item.getVendorPayoutAmount() != null)
                .map(OrderItem::getVendorPayoutAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalAssignedOrders = vendorOrderItems.size();
        long activeSkus = productRepository.countByVendorId(vendor.getId());
        long lowStockItems = productRepository.countByVendorIdAndStockLessThan(vendor.getId(), 5);

        VendorStatsResponse stats = new VendorStatsResponse(
                totalNetEarnings,
                totalAssignedOrders,
                activeSkus,
                lowStockItems
        );

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/products")
    public ResponseEntity<List<ProductResponse>> getVendorProducts(Principal principal) {
        String email = principal.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Vendor vendor = vendorRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Vendor profile not found"));

        List<Product> products = productRepository.findByVendorId(vendor.getId());
        List<ProductResponse> responses = products.stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @PostMapping(value = "/products/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadProductImage(
            @RequestParam("file") MultipartFile file) {
        try {
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String uniqueFilename = UUID.randomUUID() + extension;

            Path uploadPath = Paths.get("uploads/products");
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), filePath);

            Map<String, String> response = new HashMap<>();
            response.put("imageUrl", "/uploads/products/" + uniqueFilename);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload image: " + e.getMessage());
        }
    }

    @PostMapping("/products")
    public ResponseEntity<ProductResponse> createVendorProduct(
            @Valid @RequestBody ProductCreateRequest request,
            Principal principal) {
        String email = principal.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Auto-create vendor if user has ROLE_VENDOR but no vendor record
        Vendor vendor = vendorRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Vendor newVendor = new Vendor(user, user.getName() + "'s Store", 
                            user.getName().toLowerCase().replace(" ", "-"), user.getEmail());
                    newVendor.setCommissionRate(BigDecimal.valueOf(10.00));
                    return vendorRepository.save(newVendor);
                });

        Product product = productService.createProduct(request);
        product.setVendor(vendor);
        product.setApprovalStatus("ACTIVE");
        productRepository.save(product);

        return ResponseEntity.ok(mapToProductResponse(product));
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<ProductResponse> updateVendorProduct(
            @PathVariable UUID id,
            @Valid @RequestBody ProductUpdateRequest request,
            Principal principal) {
        String email = principal.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Vendor vendor = vendorRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Vendor profile not found"));

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (!product.getVendor().getId().equals(vendor.getId())) {
            throw new ResourceNotFoundException("Product not found or access denied");
        }

        productService.updateProduct(id, request);
        Product updatedProduct = productRepository.findById(id).get();

        return ResponseEntity.ok(mapToProductResponse(updatedProduct));
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<Void> deleteVendorProduct(
            @PathVariable UUID id,
            Principal principal) {
        String email = principal.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Vendor vendor = vendorRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Vendor profile not found"));

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (!product.getVendor().getId().equals(vendor.getId())) {
            throw new ResourceNotFoundException("Product not found or access denied");
        }

        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/orders")
    public ResponseEntity<List<OrderItem>> getVendorOrders(Principal principal) {
        String email = principal.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Vendor vendor = vendorRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Vendor profile not found"));

        List<OrderItem> orderItems = orderItemRepository.findByVendorId(vendor.getId());
        return ResponseEntity.ok(orderItems);
    }

    @PatchMapping("/orders/{orderItemId}/dispatch")
    public ResponseEntity<OrderItem> dispatchOrder(
            @PathVariable UUID orderItemId,
            Principal principal) {
        String email = principal.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Vendor vendor = vendorRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Vendor profile not found"));

        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Order item not found"));

        if (!orderItem.getVendor().getId().equals(vendor.getId())) {
            throw new ResourceNotFoundException("Order item not found or access denied");
        }

        orderItem.setFulfillmentStatus("DISPATCHED");
        orderItemRepository.save(orderItem);

        return ResponseEntity.ok(orderItem);
    }

    private ProductResponse mapToProductResponse(Product product) {
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
        
        // Include vendor name if product has a vendor
        if (product.getVendor() != null) {
            response.setVendorName(product.getVendor().getStoreName());
        }
        
        return response;
    }
}
