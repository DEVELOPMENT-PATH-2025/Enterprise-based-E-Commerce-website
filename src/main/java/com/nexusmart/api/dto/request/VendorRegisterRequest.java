package com.nexusmart.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class VendorRegisterRequest {
    @NotBlank
    private String storeName;

    @NotBlank
    private String storeSlug;

    @Email
    @NotBlank
    private String businessEmail;

    @NotBlank
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getStoreSlug() {
        return storeSlug;
    }

    public void setStoreSlug(String storeSlug) {
        this.storeSlug = storeSlug;
    }

    public String getBusinessEmail() {
        return businessEmail;
    }

    public void setBusinessEmail(String businessEmail) {
        this.businessEmail = businessEmail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
