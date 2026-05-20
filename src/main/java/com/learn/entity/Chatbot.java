package com.learn.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "chatbots")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Chatbot extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long chatbotId;

    @Column(nullable = false)
    private String chatbotName;

    /**
     * Ollama model identifier, e.g. "gpt-oss:120b-cloud"
     */
    @Column(nullable = false)
    private String modelName;

    /**
     * Sampling temperature (0.0 – 2.0).
     * Controls creativity / randomness of responses.
     */
    @Column(nullable = false)
    private Double temperature;

    /**
     * Top-K sampling: limits the token pool to the K most likely tokens.
     */
    @Column(nullable = false)
    private Integer topK;

    /**
     * Top-P (nucleus) sampling: cumulative probability threshold.
     */
    @Column(nullable = false)
    private Double topP;

    /**
     * Optional system prompt / persona description shown to the model
     * before every conversation.
     */
    @Column(columnDefinition = "TEXT")
    private String systemPrompt;

    private Boolean active;

    /**
     * One chatbot per company (unique constraint enforced at DB level).
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false, unique = true)
    private Company company;
}
