package com.learn.security;

import com.learn.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.MacAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {

    // Explicitly use HS256
    private static final MacAlgorithm ALG = Jwts.SIG.HS256;

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration}")
    private long expiration;

    /**
     * Decode Base64 secret and create signing key
     */
    private SecretKey getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generate JWT token
     */
    public String generateToken(User user) {

        Map<String, Object> claims = new HashMap<>();

        claims.put(
                "companyId",
                user.getCompany() != null
                        ? user.getCompany().getCompanyId()
                        : null
        );

        claims.put("userId", user.getUserId());
        claims.put("userType", user.getUserType());
        claims.put("userRole", user.getUserRole());

        return Jwts.builder()
                .claims(claims)
                .subject(user.getEmail())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignKey(), ALG)
                .compact();
    }

    /**
     * Extract all claims
     */
    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Extract username/email
     */
    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    /**
     * Validate token
     */
    public boolean isTokenValid(String token, User user) {
        final String username = extractUsername(token);

        return username.equals(user.getEmail())
                && !isTokenExpired(token);
    }

    /**
     * Check expiration
     */
    private boolean isTokenExpired(String token) {
        return extractClaims(token)
                .getExpiration()
                .before(new Date());
    }

    /**
     * Generate secure Base64 secret once
     * Run temporarily if needed.
     */
    public static void main(String[] args) {

        SecretKey key = Jwts.SIG.HS256.key().build();

        String base64Secret =
                Encoders.BASE64.encode(key.getEncoded());

        System.out.println(base64Secret);
    }
}