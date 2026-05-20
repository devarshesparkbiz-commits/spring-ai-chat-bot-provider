package com.learn.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiKeyResponse {

    private Long keyId;
    private String keyName;
    /** Only populated on creation — null on subsequent fetches. */
    private String plainKey;
    private String keyPrefix;
    private String allowedOrigins;
    private Boolean active;
    private LocalDateTime lastUsedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
