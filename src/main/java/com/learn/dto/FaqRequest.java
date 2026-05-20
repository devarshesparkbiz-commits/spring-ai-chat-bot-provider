package com.learn.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FaqRequest {

    private String question;

    private String answer;

    private Boolean active;

    private Long companyId;
}
