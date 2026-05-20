package com.learn.security;

import com.learn.entity.ApiKey;
import com.learn.repository.ApiKeyRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HexFormat;
import java.util.Optional;

/**
 * Handles authentication for external API requests using the X-API-Key header.
 * Only activates for requests to /api/v1/** paths.
 *
 * On success, sets a UsernamePasswordAuthenticationToken with:
 *   - principal: "api_key:{companyId}"
 *   - details:   the ApiKey entity (so the controller can read companyId)
 *   - authority: "API_CLIENT"
 */
@Slf4j
@RequiredArgsConstructor
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "X-API-Key";
    private static final String API_PATH_PREFIX = "/api/v1/";

    private final ApiKeyRepository apiKeyRepository;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Only run this filter for external API paths
        return !request.getRequestURI().startsWith(API_PATH_PREFIX);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String rawKey = request.getHeader(API_KEY_HEADER);

        if (rawKey == null || rawKey.isBlank()) {
            sendUnauthorized(response, "Missing X-API-Key header");
            return;
        }

        // Trim any accidental whitespace from the key value
        rawKey = rawKey.trim();
        log.debug("API key received, length={}, prefix={}",
                rawKey.length(), rawKey.substring(0, Math.min(rawKey.length(), 12)));

        String hashed = hash(rawKey);
        log.debug("Computed hash: {}", hashed);
        Optional<ApiKey> keyOpt = apiKeyRepository.findByKeyHashAndActiveTrue(hashed);

        if (keyOpt.isEmpty()) {
            log.warn("No active API key found for hash: {}", hashed);
            sendUnauthorized(response, "Invalid or revoked API key");
            return;
        }

        log.debug("API key validated for company: {}",
                keyOpt.get().getCompany().getCompanyId());

        ApiKey apiKey = keyOpt.get();

        // Optional: validate allowed origins
        String origin = request.getHeader("Origin");
        if (!isOriginAllowed(apiKey, origin)) {
            sendForbidden(response, "Origin not allowed for this API key");
            return;
        }

        // Update last-used timestamp asynchronously (best-effort, non-blocking)
        apiKey.setLastUsedAt(LocalDateTime.now());
        apiKeyRepository.save(apiKey);

        // Set authentication in security context
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        "api_key:" + apiKey.getCompany().getCompanyId(),
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority("API_CLIENT"))
                );
        auth.setDetails(apiKey);   // controllers read companyId from here
        SecurityContextHolder.getContext().setAuthentication(auth);

        filterChain.doFilter(request, response);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private boolean isOriginAllowed(ApiKey apiKey, String requestOrigin) {
        String allowed = apiKey.getAllowedOrigins();
        // No restriction configured → allow all
        if (allowed == null || allowed.isBlank()) return true;
        // No origin header (server-to-server call) → allow
        if (requestOrigin == null || requestOrigin.isBlank()) return true;
        for (String o : allowed.split(",")) {
            if (o.trim().equalsIgnoreCase(requestOrigin.trim())) return true;
        }
        return false;
    }

    private String hash(String plain) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(plain.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    private void sendUnauthorized(HttpServletResponse response, String message)
            throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"" + message + "\"}");
    }

    private void sendForbidden(HttpServletResponse response, String message)
            throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"" + message + "\"}");
    }
}
