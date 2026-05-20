package com.learn.controller;

import com.learn.dto.ApiKeyRequest;
import com.learn.response.ApiKeyResponse;
import com.learn.response.CommonResponse;
import com.learn.service.ApiKeyService;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API Key management for COMPANY_ADMIN.
 * Base path: /api-keys
 */
@RestController
@RequestMapping("/api-keys")
@RequiredArgsConstructor
public class ApiKeyController {

    private final ApiKeyService apiKeyService;

    private Long getCompanyIdFromAuth(Authentication auth) {
        if (auth.getDetails() instanceof Claims claims) {
            Object raw = claims.get("companyId");
            if (raw == null) throw new RuntimeException("No companyId in token");
            return ((Number) raw).longValue();
        }
        throw new RuntimeException("Unable to resolve companyId from token");
    }

    /**
     * Generate a new API key.
     * The plain key is returned ONCE in the response — store it securely.
     *
     * POST /api-keys
     */
    @PostMapping
    public ApiKeyResponse createKey(
            @Valid @RequestBody ApiKeyRequest request,
            Authentication auth
    ) {
        Long companyId = getCompanyIdFromAuth(auth);
        return apiKeyService.createKey(companyId, request);
    }

    /**
     * List all API keys for the company (plain key never returned here).
     *
     * GET /api-keys
     */
    @GetMapping
    public List<ApiKeyResponse> listKeys(Authentication auth) {
        Long companyId = getCompanyIdFromAuth(auth);
        return apiKeyService.listKeys(companyId);
    }

    /**
     * Update key name or allowed origins.
     *
     * PUT /api-keys/{keyId}
     */
    @PutMapping("/{keyId:\\d+}")
    public CommonResponse updateKey(
            @PathVariable Long keyId,
            @Valid @RequestBody ApiKeyRequest request,
            Authentication auth
    ) {
        Long companyId = getCompanyIdFromAuth(auth);
        return apiKeyService.updateKey(keyId, companyId, request);
    }

    /**
     * Revoke an API key.
     *
     * DELETE /api-keys/{keyId}
     */
    @DeleteMapping("/{keyId:\\d+}")
    public CommonResponse revokeKey(
            @PathVariable Long keyId,
            Authentication auth
    ) {
        Long companyId = getCompanyIdFromAuth(auth);
        return apiKeyService.revokeKey(keyId, companyId);
    }

    /**
     * Regenerate: revoke the old key and issue a new one with the same name/origins.
     * The new plain key is returned ONCE.
     *
     * POST /api-keys/{keyId}/regenerate
     */
    @PostMapping("/{keyId:\\d+}/regenerate")
    public ApiKeyResponse regenerateKey(
            @PathVariable Long keyId,
            Authentication auth
    ) {
        Long companyId = getCompanyIdFromAuth(auth);
        return apiKeyService.regenerateKey(keyId, companyId);
    }
}
