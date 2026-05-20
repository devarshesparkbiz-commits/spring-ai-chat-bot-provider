package com.learn.serviceimpl;

import com.learn.dto.ApiKeyRequest;
import com.learn.entity.ApiKey;
import com.learn.entity.Company;
import com.learn.repository.ApiKeyRepository;
import com.learn.repository.CompanyRepository;
import com.learn.response.ApiKeyResponse;
import com.learn.response.CommonResponse;
import com.learn.service.ApiKeyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ApiKeyServiceImpl implements ApiKeyService {

    private final ApiKeyRepository  apiKeyRepository;
    private final CompanyRepository companyRepository;

    // ── Key generation ────────────────────────────────────────────────────────

    /**
     * Generates a cryptographically random API key in the format:
     *   sk_live_{32 random URL-safe base64 chars}
     */
    private String generatePlainKey() {
        byte[] bytes = new byte[24];
        new SecureRandom().nextBytes(bytes);
        String random = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        return "sk_live_" + random;
    }

    /** SHA-256 hex hash of the plain key. */
    private String hash(String plainKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(plainKey.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    // ── Mapper ────────────────────────────────────────────────────────────────

    private ApiKeyResponse toResponse(ApiKey key, String plainKey) {
        return ApiKeyResponse.builder()
                .keyId(key.getKeyId())
                .keyName(key.getKeyName())
                .plainKey(plainKey)          // non-null only on creation
                .keyPrefix(key.getKeyPrefix())
                .allowedOrigins(key.getAllowedOrigins())
                .active(key.getActive())
                .lastUsedAt(key.getLastUsedAt())
                .createdAt(key.getCreatedAt())
                .updatedAt(key.getUpdatedAt())
                .build();
    }

    // ── Operations ────────────────────────────────────────────────────────────

    @Override
    public ApiKeyResponse createKey(Long companyId, ApiKeyRequest request) {

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        String plain  = generatePlainKey();
        String hashed = hash(plain);
        String prefix = plain.substring(0, Math.min(plain.length(), 16));

        ApiKey key = ApiKey.builder()
                .keyName(request.getKeyName())
                .keyHash(hashed)
                .keyPrefix(prefix)
                .allowedOrigins(request.getAllowedOrigins())
                .active(true)
                .company(company)
                .build();

        apiKeyRepository.save(key);

        // Return the plain key ONCE — it is never retrievable again
        return toResponse(key, plain);
    }

    @Override
    public List<ApiKeyResponse> listKeys(Long companyId) {
        return apiKeyRepository.findByCompany_CompanyId(companyId)
                .stream()
                .map(k -> toResponse(k, null))   // plain key never returned on list
                .toList();
    }

    @Override
    public CommonResponse revokeKey(Long keyId, Long companyId) {
        ApiKey key = findOwnedKey(keyId, companyId);
        key.setActive(false);
        apiKeyRepository.save(key);
        return CommonResponse.builder().message("API key revoked successfully").build();
    }

    @Override
    public CommonResponse updateKey(Long keyId, Long companyId, ApiKeyRequest request) {
        ApiKey key = findOwnedKey(keyId, companyId);
        if (request.getKeyName() != null) key.setKeyName(request.getKeyName());
        if (request.getAllowedOrigins() != null) key.setAllowedOrigins(request.getAllowedOrigins());
        apiKeyRepository.save(key);
        return CommonResponse.builder().message("API key updated successfully").build();
    }

    @Override
    @Transactional
    public ApiKeyResponse regenerateKey(Long keyId, Long companyId) {
        ApiKey old = findOwnedKey(keyId, companyId);

        // Read company while still in transaction (lazy proxy is live)
        Company company       = old.getCompany();
        String  oldKeyName    = old.getKeyName();
        String  oldOrigins    = old.getAllowedOrigins();

        // Revoke the old key
        old.setActive(false);
        apiKeyRepository.save(old);

        // Issue a fresh key with the same name and allowed origins
        String plain  = generatePlainKey();
        String hashed = hash(plain);
        String prefix = plain.substring(0, Math.min(plain.length(), 16));

        ApiKey fresh = ApiKey.builder()
                .keyName(oldKeyName)
                .keyHash(hashed)
                .keyPrefix(prefix)
                .allowedOrigins(oldOrigins)
                .active(true)
                .company(company)
                .build();

        apiKeyRepository.save(fresh);

        return toResponse(fresh, plain);
    }

    // ── Internal ──────────────────────────────────────────────────────────────

    private ApiKey findOwnedKey(Long keyId, Long companyId) {
        ApiKey key = apiKeyRepository.findByIdWithCompany(keyId)
                .orElseThrow(() -> new RuntimeException("API key not found"));
        if (!key.getCompany().getCompanyId().equals(companyId)) {
            throw new RuntimeException("Access denied");
        }
        return key;
    }
}
