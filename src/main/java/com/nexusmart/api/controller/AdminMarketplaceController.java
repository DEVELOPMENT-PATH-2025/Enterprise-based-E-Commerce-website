package com.nexusmart.api.controller;

import com.nexusmart.api.dto.response.MarketplaceStatsResponse;
import com.nexusmart.api.dto.response.OrderResponse;
import com.nexusmart.api.dto.response.ProductResponse;
import com.nexusmart.api.dto.response.UserResponse;
import com.nexusmart.api.dto.response.VendorResponse;
import com.nexusmart.api.entity.Order;
import com.nexusmart.api.entity.OrderItem;
import com.nexusmart.api.entity.Product;
import com.nexusmart.api.entity.User;
import com.nexusmart.api.entity.Vendor;
import com.nexusmart.api.entity.VendorStatus;
import com.nexusmart.api.exception.ResourceNotFoundException;
import com.nexusmart.api.repository.OrderItemRepository;
import com.nexusmart.api.repository.OrderRepository;
import com.nexusmart.api.repository.ProductRepository;
import com.nexusmart.api.repository.UserRepository;
import com.nexusmart.api.repository.VendorRepository;
import com.nexusmart.api.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class AdminMarketplaceController {

    private final VendorRepository vendorRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ProductService productService;

    public AdminMarketplaceController(
            VendorRepository vendorRepository,
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            ProductRepository productRepository,
            UserRepository userRepository,
            ProductService productService) {
        this.vendorRepository = vendorRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.productService = productService;
    }

    @GetMapping("/marketplace/stats")
    public ResponseEntity<MarketplaceStatsResponse> getMarketplaceStats() {
        try {
            // Platform GMV: Sum of all orders.total_amount where status is PAID
            BigDecimal platformGmv = orderRepository.findAll().stream()
                    .filter(order -> order != null && "PAID".equals(order.getStatus()))
                    .map(order -> order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Platform Net Commission: Sum of all commissions from vendor sales + 100% of platform product sales
            List<OrderItem> allOrderItems = orderItemRepository.findAll();
            BigDecimal platformNetCommission = BigDecimal.ZERO;

            for (OrderItem item : allOrderItems) {
                if (item == null) continue;

                BigDecimal itemTotal = BigDecimal.ZERO;
                if (item.getPriceSnapshot() != null && item.getQuantity() != null) {
                    itemTotal = item.getPriceSnapshot().multiply(BigDecimal.valueOf(item.getQuantity()));
                }

                if (item.getVendor() != null) {
                    // Vendor sale: commission = itemTotal - vendorPayout
                    BigDecimal vendorPayout = item.getVendorPayoutAmount() != null ? item.getVendorPayoutAmount() : BigDecimal.ZERO;
                    BigDecimal commission = itemTotal.subtract(vendorPayout);
                    platformNetCommission = platformNetCommission.add(commission);
                } else {
                    // Platform owned item: platform keeps 100%
                    platformNetCommission = platformNetCommission.add(itemTotal);
                }
            }

            long totalRegisteredVendors = vendorRepository.count();
            long totalActiveVendors = vendorRepository.countByStatus("ACTIVE");
            long pendingVendorApprovals = vendorRepository.countByStatus("PENDING_APPROVAL");
            long totalOrders = orderRepository.countAll();

            MarketplaceStatsResponse stats = new MarketplaceStatsResponse(
                    platformGmv,
                    platformNetCommission,
                    totalRegisteredVendors,
                    totalActiveVendors,
                    pendingVendorApprovals,
                    totalOrders
            );

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            // Return zero values on any error to prevent 500
            MarketplaceStatsResponse fallbackStats = new MarketplaceStatsResponse(
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    0L,
                    0L,
                    0L,
                    0L
            );
            return ResponseEntity.ok(fallbackStats);
        }
    }

    @GetMapping("/marketplace/vendors")
    public ResponseEntity<List<VendorResponse>> getAllVendors(
            @RequestParam(required = false) String status) {
        try {
            List<Vendor> vendors;
            if (status != null && !status.isEmpty()) {
                vendors = vendorRepository.findByStatus(status);
            } else {
                vendors = vendorRepository.findAll();
            }
            
            List<VendorResponse> responses = vendors.stream()
                    .map(this::mapToVendorResponse)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return ResponseEntity.ok(List.of());
        }
    }

    @PatchMapping("/marketplace/vendors/{vendorId}/approve")
    public ResponseEntity<Vendor> approveVendor(@PathVariable UUID vendorId) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new RuntimeException("Vendor not found"));

        vendor.setStatus(VendorStatus.ACTIVE);
        Vendor savedVendor = vendorRepository.save(vendor);

        return ResponseEntity.ok(savedVendor);
    }

    @PatchMapping("/marketplace/vendors/{vendorId}/suspend")
    public ResponseEntity<Vendor> suspendVendor(@PathVariable UUID vendorId) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new RuntimeException("Vendor not found"));

        vendor.setStatus(VendorStatus.SUSPENDED);
        Vendor savedVendor = vendorRepository.save(vendor);

        return ResponseEntity.ok(savedVendor);
    }

    @PutMapping("/marketplace/vendors/{vendorId}/commission")
    public ResponseEntity<Vendor> updateVendorCommission(
            @PathVariable UUID vendorId,
            @RequestBody CommissionUpdateRequest request) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new RuntimeException("Vendor not found"));

        if (request.getCommissionRate() == null || request.getCommissionRate().compareTo(BigDecimal.ZERO) < 0 || request.getCommissionRate().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new RuntimeException("Invalid commission rate. Must be between 0 and 100.");
        }

        vendor.setCommissionRate(request.getCommissionRate());
        Vendor savedVendor = vendorRepository.save(vendor);

        return ResponseEntity.ok(savedVendor);
    }

    public static class CommissionUpdateRequest {
        private BigDecimal commissionRate;

        public BigDecimal getCommissionRate() {
            return commissionRate;
        }

        public void setCommissionRate(BigDecimal commissionRate) {
            this.commissionRate = commissionRate;
        }
    }

    // Global Catalog Moderation Endpoints
    @GetMapping("/marketplace/products")
    public ResponseEntity<List<ProductResponse>> getAllMarketplaceProducts(
            @RequestParam(required = false) UUID vendorId) {
        List<Product> products;
        if (vendorId != null) {
            products = productRepository.findByVendorId(vendorId);
        } else {
            products = productRepository.findAll();
        }

        List<ProductResponse> responses = products.stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/marketplace/products/{productId}")
    public ResponseEntity<Void> deleteMarketplaceProduct(@PathVariable UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        productService.deleteProduct(productId);
        return ResponseEntity.noContent().build();
    }

    // Multi-Vendor Order Audit Endpoint
    @GetMapping("/marketplace/orders")
    public ResponseEntity<List<OrderResponse>> getAllMarketplaceOrders() {
        List<Order> orders = orderRepository.findAll();
        
        List<OrderResponse> responses = orders.stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }

    private VendorResponse mapToVendorResponse(Vendor vendor) {
        VendorResponse response = new VendorResponse();
        response.setId(vendor.getId());
        response.setStoreName(vendor.getStoreName());
        response.setStoreSlug(vendor.getStoreSlug());
        response.setBusinessEmail(vendor.getBusinessEmail());
        response.setStatus(vendor.getStatus() != null ? vendor.getStatus().name() : null);
        response.setCommissionRate(vendor.getCommissionRate());
        response.setCreatedAt(vendor.getCreatedAt());
        return response;
    }

    private OrderResponse mapToOrderResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setUserId(order.getUserId());
        response.setTotalAmount(order.getTotalAmount());
        response.setStatus(order.getStatus());
        response.setCreatedAt(order.getCreatedAt());
        
        if (order.getItems() != null) {
            List<OrderResponse.OrderItemResponse> itemResponses = order.getItems().stream()
                    .map(item -> new OrderResponse.OrderItemResponse(
                            item.getProductId(),
                            item.getProductId() != null ? item.getProductId().toString() : "Product",
                            item.getQuantity(),
                            item.getPriceSnapshot()
                    ))
                    .collect(Collectors.toList());
            response.setItems(itemResponses);
        }
        
        return response;
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

    // User Management Endpoints
    @GetMapping("/users")
    public ResponseEntity<List<Map<String, Object>>> getAdminUsers(
            @RequestParam(required = false) String role) {
        try {
            // 1. Fetch all users from the PostgreSQL 'users' table
            List<User> allUsers = userRepository.findAll();

            // 2. Filter ONLY if a specific role is requested and it is NOT "ALL"
            List<User> filteredUsers = allUsers;
            if (role != null && !role.isBlank() && !"ALL".equalsIgnoreCase(role)) {
                filteredUsers = allUsers.stream()
                        .filter(u -> u.getRole() != null && u.getRole().equalsIgnoreCase(role))
                        .collect(Collectors.toList());
            }

            // 3. Map to clean JSON response (matching the 9 rows in pgAdmin)
            List<Map<String, Object>> response = filteredUsers.stream().map(u -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", u.getId() != null ? u.getId().toString() : "");
                map.put("name", u.getName() != null && !u.getName().isBlank() ? u.getName() : "NexusMart User");
                map.put("email", u.getEmail());
                map.put("role", u.getRole() != null ? u.getRole() : "ROLE_CUSTOMER");
                map.put("status", u.getStatus() != null ? u.getStatus() : "ACTIVE");
                map.put("createdAt", u.getCreatedAt() != null ? u.getCreatedAt().toString() : "");
                return map;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.ok(List.of());
        }
    }

    @PatchMapping("/users/{userId}/status")
    public ResponseEntity<UserResponse> toggleUserStatus(
            @PathVariable UUID userId,
            Authentication authentication) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Prevent admin from suspending themselves
        String adminEmail = authentication.getName();
        if (user.getEmail().equals(adminEmail)) {
            throw new RuntimeException("You cannot modify your own account status");
        }

        // Toggle status
        String newStatus = "ACTIVE".equals(user.getStatus()) ? "SUSPENDED" : "ACTIVE";
        user.setStatus(newStatus);
        User savedUser = userRepository.save(user);

        return ResponseEntity.ok(mapToUserResponse(savedUser));
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable UUID userId,
            Authentication authentication) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Prevent admin from deleting themselves
        String adminEmail = authentication.getName();
        if (user.getEmail().equals(adminEmail)) {
            throw new RuntimeException("You cannot delete your own account");
        }

        // Delete associated vendor if exists
        vendorRepository.findByUserId(userId).ifPresent(vendor -> {
            vendorRepository.delete(vendor);
        });

        // Delete user
        userRepository.delete(user);

        return ResponseEntity.noContent().build();
    }

    private UserResponse mapToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setStatus(user.getStatus());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }
}
