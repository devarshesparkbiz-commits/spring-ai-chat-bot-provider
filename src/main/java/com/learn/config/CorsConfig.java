package com.learn.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        // ── Internal admin panel (React frontend) ─────────────────────────────
        CorsConfiguration internal = new CorsConfiguration();
        internal.setAllowedOrigins(List.of("http://localhost:5173"));
        internal.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        internal.setAllowedHeaders(List.of("*"));
        internal.setAllowCredentials(true);
        source.registerCorsConfiguration("/**", internal);

        // ── External public API — allow any origin ────────────────────────────
        // Companies embed this in their own websites/apps from any domain.
        // Per-key origin restriction is enforced in ApiKeyAuthenticationFilter.
        CorsConfiguration external = new CorsConfiguration();
        external.setAllowedOriginPatterns(List.of("*"));
        external.setAllowedMethods(List.of("GET", "POST", "OPTIONS"));
        external.setAllowedHeaders(List.of("Content-Type", "X-API-Key"));
        external.setAllowCredentials(false);
        source.registerCorsConfiguration("/api/v1/**", external);

        return source;
    }
}
