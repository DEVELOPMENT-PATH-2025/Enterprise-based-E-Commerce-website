package com.nexusmart.api.dto.request;

import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.URL;
import java.math.BigDecimal;

public class ProductUpdateRequest {

    @NotBlank(message = "Product name is required")
    @Size(min = 3, max = 255, message = "Product name must be between 3 and 255 characters")
    private String name;

    @Size(min = 10, max = 2000, message = "Description must be between 10 and 2000 characters")
    private String description;

    @DecimalMin(value = "0.01", message = "Price must be at least 0.01")
    @Digits(integer = 8, fraction = 2, message = "Price must have at most 2 decimal places")
    private BigDecimal price;

    @Min(value = 0, message = "Stock cannot be negative")
    private Integer stock;

    @Size(max = 100, message = "Category must not exceed 100 characters")
    private String category;

    @URL(message = "Image URL must be valid")
    private String imageUrl;

    public ProductUpdateRequest() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
