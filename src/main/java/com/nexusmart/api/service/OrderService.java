package com.nexusmart.api.service;

import com.nexusmart.api.dto.request.CheckoutRequest;
import com.nexusmart.api.dto.response.OrderResponse;
import com.nexusmart.api.entity.Order;
import com.nexusmart.api.entity.OrderItem;
import com.nexusmart.api.entity.Product;
import com.nexusmart.api.exception.ResourceNotFoundException;
import com.nexusmart.api.repository.OrderRepository;
import com.nexusmart.api.repository.ProductRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    public OrderResponse checkout(CheckoutRequest request, UUID userId) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        for (CheckoutRequest.OrderItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + itemRequest.getProductId()));
            
            if (product.getStock() < itemRequest.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }
            
            totalAmount = totalAmount.add(product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity())));
        }

        Order order = new Order(userId, totalAmount);
        order.setStatus("PAID");
        
        for (CheckoutRequest.OrderItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
            
            OrderItem orderItem = new OrderItem(
                    order,
                    product.getId(),
                    itemRequest.getQuantity(),
                    product.getPrice()
            );
            order.getItems().add(orderItem);
        }
        
        Order savedOrder = orderRepository.save(order);
        
        for (OrderItem item : savedOrder.getItems()) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
            
            product.setStock(product.getStock() - item.getQuantity());
            productRepository.save(product);
        }
        
        return mapToResponse(savedOrder);
    }

    public List<OrderResponse> getCustomerOrders(UUID userId) {
        List<Order> orders = orderRepository.findByUserId(userId);
        return orders.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public OrderResponse getOrderById(UUID orderId, UUID userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        
        if (!order.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Order not found");
        }
        
        return mapToResponse(order);
    }

    private OrderResponse mapToResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setUserId(order.getUserId());
        response.setTotalAmount(order.getTotalAmount());
        response.setStatus(order.getStatus());
        response.setCreatedAt(order.getCreatedAt());
        
        List<OrderResponse.OrderItemResponse> items = order.getItems().stream()
                .map(item -> {
                    Product product = productRepository.findById(item.getProductId()).orElse(null);
                    String productName = product != null ? product.getName() : "Unknown";
                    
                    OrderResponse.OrderItemResponse itemResponse = new OrderResponse.OrderItemResponse(
                        item.getProductId(),
                        productName,
                        item.getQuantity(),
                        item.getPriceSnapshot()
                    );
                    return itemResponse;
                })
                .collect(Collectors.toList());
        
        response.setItems(items);
        return response;
    }
}
