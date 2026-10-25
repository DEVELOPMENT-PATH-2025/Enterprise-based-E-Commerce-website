package com.nexusmart.api.dto.response;

import java.math.BigDecimal;

public class VendorStatsResponse {
    private BigDecimal totalNetEarnings;
    private long totalAssignedOrders;
    private long activeSkus;
    private long lowStockItems;

    public VendorStatsResponse() {}

    public VendorStatsResponse(BigDecimal totalNetEarnings, long totalAssignedOrders, long activeSkus, long lowStockItems) {
        this.totalNetEarnings = totalNetEarnings;
        this.totalAssignedOrders = totalAssignedOrders;
        this.activeSkus = activeSkus;
        this.lowStockItems = lowStockItems;
    }

    public BigDecimal getTotalNetEarnings() {
        return totalNetEarnings;
    }

    public void setTotalNetEarnings(BigDecimal totalNetEarnings) {
        this.totalNetEarnings = totalNetEarnings;
    }

    public long getTotalAssignedOrders() {
        return totalAssignedOrders;
    }

    public void setTotalAssignedOrders(long totalAssignedOrders) {
        this.totalAssignedOrders = totalAssignedOrders;
    }

    public long getActiveSkus() {
        return activeSkus;
    }

    public void setActiveSkus(long activeSkus) {
        this.activeSkus = activeSkus;
    }

    public long getLowStockItems() {
        return lowStockItems;
    }

    public void setLowStockItems(long lowStockItems) {
        this.lowStockItems = lowStockItems;
    }
}
