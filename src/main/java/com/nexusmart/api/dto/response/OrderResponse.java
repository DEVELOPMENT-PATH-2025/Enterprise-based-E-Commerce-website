package com.nexusmart.api.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class OrderResponse {
    private UUID id;
    private UUID userId;
    private BigDecimal totalAmount;
    private String status;
    private LocalDateTime createdAt;
    private List<OrderItemResponse> items;

    public OrderResponse() {}

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<OrderItemResponse> getItems() {
        return items;
    }

    public void setItems(List<OrderItemResponse> items) {
        this.items = items;
    }

    public static class OrderItemResponse {
        private UUID productId;
        private String productName;
        private Integer quantity;
        private BigDecimal priceSnapshot;

        public OrderItemResponse() {}

        public OrderItemResponse(UUID productId, String productName, Integer quantity, BigDecimal priceSnapshot) {
            this.productId = productId;
            this.productName = productName;
            this.quantity = quantity;
            this.priceSnapshot = priceSnapshot;
        }

        public UUID getProductId() {
            return productId;
        }

        public void setProductId(UUID productId) {
            this.productId = productId;
        }

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        public BigDecimal getPriceSnapshot() {
            return priceSnapshot;
        }

        public void setPriceSnapshot(BigDecimal priceSnapshot) {
            this.priceSnapshot = priceSnapshot;
        }
    }
}
