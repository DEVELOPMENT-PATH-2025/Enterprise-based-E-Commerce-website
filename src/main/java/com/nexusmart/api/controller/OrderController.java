package com.nexusmart.api.controller;

import com.nexusmart.api.dto.request.CheckoutRequest;
import com.nexusmart.api.dto.response.OrderResponse;
import com.nexusmart.api.security.UserPrincipal;
import com.nexusmart.api.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/checkout")
    public ResponseEntity<OrderResponse> checkout(
            @Valid @RequestBody CheckoutRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        OrderResponse order = orderService.checkout(request, userPrincipal.getUserId());
        return ResponseEntity.ok(order);
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getCustomerOrders(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<OrderResponse> orders = orderService.getCustomerOrders(userPrincipal.getUserId());
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        OrderResponse order = orderService.getOrderById(id, userPrincipal.getUserId());
        return ResponseEntity.ok(order);
    }
}
