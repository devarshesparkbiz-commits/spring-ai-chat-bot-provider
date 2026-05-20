package com.learn.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FaqResponse {

    private Long faqId;

    private String question;

    private String answer;

    private Boolean active;

    private Long companyId;

    private String companyName;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
