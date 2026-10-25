package com.nexusmart.api.dto.response;

import java.math.BigDecimal;

public class MarketplaceStatsResponse {
    private BigDecimal platformGmv;
    private BigDecimal platformNetCommission;
    private long totalRegisteredVendors;
    private long totalActiveVendors;
    private long pendingVendorApprovals;
    private long totalOrders;

    public MarketplaceStatsResponse() {}

    public MarketplaceStatsResponse(BigDecimal platformGmv, BigDecimal platformNetCommission, long totalRegisteredVendors, long totalActiveVendors, long pendingVendorApprovals, long totalOrders) {
        this.platformGmv = platformGmv;
        this.platformNetCommission = platformNetCommission;
        this.totalRegisteredVendors = totalRegisteredVendors;
        this.totalActiveVendors = totalActiveVendors;
        this.pendingVendorApprovals = pendingVendorApprovals;
        this.totalOrders = totalOrders;
    }

    public BigDecimal getPlatformGmv() {
        return platformGmv;
    }

    public void setPlatformGmv(BigDecimal platformGmv) {
        this.platformGmv = platformGmv;
    }

    public BigDecimal getPlatformNetCommission() {
        return platformNetCommission;
    }

    public void setPlatformNetCommission(BigDecimal platformNetCommission) {
        this.platformNetCommission = platformNetCommission;
    }

    public long getTotalRegisteredVendors() {
        return totalRegisteredVendors;
    }

    public void setTotalRegisteredVendors(long totalRegisteredVendors) {
        this.totalRegisteredVendors = totalRegisteredVendors;
    }

    public long getPendingVendorApprovals() {
        return pendingVendorApprovals;
    }

    public void setPendingVendorApprovals(long pendingVendorApprovals) {
        this.pendingVendorApprovals = pendingVendorApprovals;
    }

    public long getTotalActiveVendors() {
        return totalActiveVendors;
    }

    public void setTotalActiveVendors(long totalActiveVendors) {
        this.totalActiveVendors = totalActiveVendors;
    }

    public long getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(long totalOrders) {
        this.totalOrders = totalOrders;
    }
}
