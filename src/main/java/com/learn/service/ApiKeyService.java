package com.learn.service;

import com.learn.dto.ApiKeyRequest;
import com.learn.response.ApiKeyResponse;
import com.learn.response.CommonResponse;

import java.util.List;

public interface ApiKeyService {

    /** Generate a new API key for the company. Returns the plain key ONCE. */
    ApiKeyResponse createKey(Long companyId, ApiKeyRequest request);

    /** List all keys for a company (plain key is never returned here). */
    List<ApiKeyResponse> listKeys(Long companyId);

    /** Revoke (soft-delete) a key. */
    CommonResponse revokeKey(Long keyId, Long companyId);

    /** Update key name / allowed origins. */
    CommonResponse updateKey(Long keyId, Long companyId, ApiKeyRequest request);

    /**
     * Regenerate: revoke the old key and issue a brand-new one with the same name/origins.
     * Returns the new plain key ONCE.
     */
    ApiKeyResponse regenerateKey(Long keyId, Long companyId);
}
