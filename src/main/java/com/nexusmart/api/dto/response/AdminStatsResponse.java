package com.nexusmart.api.dto.response;

import java.math.BigDecimal;

public class AdminStatsResponse {
    private BigDecimal totalRevenue;
    private Long totalOrders;
    private Long activeSkus;
    private Long lowStockItems;

    public AdminStatsResponse() {}

    public AdminStatsResponse(BigDecimal totalRevenue, Long totalOrders, Long activeSkus, Long lowStockItems) {
        this.totalRevenue = totalRevenue;
        this.totalOrders = totalOrders;
        this.activeSkus = activeSkus;
        this.lowStockItems = lowStockItems;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public Long getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(Long totalOrders) {
        this.totalOrders = totalOrders;
    }

    public Long getActiveSkus() {
        return activeSkus;
    }

    public void setActiveSkus(Long activeSkus) {
        this.activeSkus = activeSkus;
    }

    public Long getLowStockItems() {
        return lowStockItems;
    }

    public void setLowStockItems(Long lowStockItems) {
        this.lowStockItems = lowStockItems;
    }
}
