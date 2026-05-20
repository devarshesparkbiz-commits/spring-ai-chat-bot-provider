package com.learn.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RagDocumentRequest {

    @NotBlank(message = "Document title is required")
    private String documentTitle;

    @NotBlank(message = "Content is required")
    private String content;

    private Boolean active;
}
