package com.learn.service;

import com.learn.dto.RagDocumentRequest;
import com.learn.response.CommonResponse;
import com.learn.response.PaginationResponse;
import com.learn.response.RagDocumentResponse;
import org.springframework.web.multipart.MultipartFile;

public interface RagDocumentService {

    /** Add a plain-text RAG document for a company. */
    CommonResponse addTextDocument(Long companyId, RagDocumentRequest request);

    /** Upload a file (PDF / DOCX / TXT) and extract its text as a RAG document. */
    CommonResponse uploadFileDocument(Long companyId, String title, MultipartFile file);

    /** Update an existing RAG document's content or title. */
    CommonResponse updateDocument(Long documentId, RagDocumentRequest request);

    /** Soft-delete a RAG document. */
    CommonResponse deleteDocument(Long documentId);

    /** Paginated list of RAG documents for a company. */
    PaginationResponse<RagDocumentResponse> getDocumentsByCompany(
            Long companyId, Integer pageNumber, Integer pageSize);

    /** Get a single document by ID. */
    RagDocumentResponse getDocument(Long documentId);
}
