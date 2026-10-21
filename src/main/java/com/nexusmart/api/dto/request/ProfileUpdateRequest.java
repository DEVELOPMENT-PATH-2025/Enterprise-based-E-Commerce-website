package com.nexusmart.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProfileUpdateRequest {
    @NotBlank(message = "Name cannot be empty")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    private String currentPassword; // Optional: required only if changing password

    @Size(min = 6, message = "New password must be at least 6 characters")
    private String newPassword;     // Optional
}
