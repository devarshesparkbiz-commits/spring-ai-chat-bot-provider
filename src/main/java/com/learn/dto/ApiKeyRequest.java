package com.learn.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiKeyRequest {

    @NotBlank(message = "Key name is required")
    private String keyName;

    /**
     * Optional comma-separated allowed origins.
     * Leave blank to allow all origins.
     * Example: "https://myapp.com,https://staging.myapp.com"
     */
    private String allowedOrigins;
}
