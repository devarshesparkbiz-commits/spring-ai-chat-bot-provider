package com.learn.response;

import com.learn.enums.RagDocumentType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RagDocumentResponse {

    private Long documentId;
    private String documentTitle;
    private String content;
    private RagDocumentType documentType;
    private Boolean active;
    private Long companyId;
    private String companyName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
