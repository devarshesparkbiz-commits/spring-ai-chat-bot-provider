package com.learn.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(name = "api_keys")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ApiKey extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long keyId;

    /** Human-readable label, e.g. "Production Website", "Mobile App" */
    @Column(nullable = false)
    private String keyName;

    /**
     * The actual key value shown to the admin ONCE on creation.
     * Stored as a SHA-256 hex hash — never stored in plain text.
     */
    @Column(nullable = false, unique = true)
    private String keyHash;

    /**
     * First 8 chars of the plain key (prefix) stored for display purposes,
     * e.g. "sk_live_ab12cd34…"  so admins can identify which key is which.
     */
    @Column(nullable = false, length = 20)
    private String keyPrefix;

    /** Optional comma-separated list of allowed origins, e.g. "https://myapp.com" */
    @Column(columnDefinition = "TEXT")
    private String allowedOrigins;

    private Boolean active;

    private LocalDateTime lastUsedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;
}
