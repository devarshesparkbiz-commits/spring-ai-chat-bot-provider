package com.learn.controller;

import com.learn.dto.RagDocumentRequest;
import com.learn.response.CommonResponse;
import com.learn.response.PaginationResponse;
import com.learn.response.RagDocumentResponse;
import com.learn.service.RagDocumentService;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * RAG document management endpoints.
 *
 * SUPER_ADMIN   → /rag/admin/**
 * COMPANY_ADMIN → /rag/my-company/**
 */
@RestController
@RequestMapping("/rag")
@RequiredArgsConstructor
public class RagDocumentController {

    private final RagDocumentService ragDocumentService;

    // ── Helper ────────────────────────────────────────────────────────────────

    private Long getCompanyIdFromAuth(Authentication auth) {
        if (auth.getDetails() instanceof Claims claims) {
            Object raw = claims.get("companyId");
            if (raw == null) throw new RuntimeException("No companyId in token");
            return ((Number) raw).longValue();
        }
        throw new RuntimeException("Unable to resolve companyId from token");
    }

    // ── SUPER_ADMIN endpoints ─────────────────────────────────────────────────

    @PostMapping("/admin/company/{companyId:\\d+}/text")
    public CommonResponse addTextDocumentForCompany(
            @PathVariable Long companyId,
            @Valid @RequestBody RagDocumentRequest request
    ) {
        return ragDocumentService.addTextDocument(companyId, request);
    }

    @PostMapping("/admin/company/{companyId:\\d+}/file")
    public CommonResponse uploadFileForCompany(
            @PathVariable Long companyId,
            @RequestParam(required = false) String title,
            @RequestPart("file") MultipartFile file
    ) {
        return ragDocumentService.uploadFileDocument(companyId, title, file);
    }

    @GetMapping("/admin/company/{companyId:\\d+}")
    public PaginationResponse<RagDocumentResponse> getDocumentsForCompany(
            @PathVariable Long companyId,
            @RequestParam(defaultValue = "0") Integer pageNumber,
            @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        return ragDocumentService.getDocumentsByCompany(companyId, pageNumber, pageSize);
    }

    // ── Shared (admin + company admin) ────────────────────────────────────────

    @GetMapping("/{documentId:\\d+}")
    public RagDocumentResponse getDocument(@PathVariable Long documentId) {
        return ragDocumentService.getDocument(documentId);
    }

    @PutMapping("/{documentId:\\d+}")
    public CommonResponse updateDocument(
            @PathVariable Long documentId,
            @Valid @RequestBody RagDocumentRequest request
    ) {
        return ragDocumentService.updateDocument(documentId, request);
    }

    @DeleteMapping("/{documentId:\\d+}")
    public CommonResponse deleteDocument(@PathVariable Long documentId) {
        return ragDocumentService.deleteDocument(documentId);
    }

    // ── COMPANY_ADMIN endpoints (companyId from JWT) ──────────────────────────

    @PostMapping("/my-company/text")
    public CommonResponse addTextDocument(
            @Valid @RequestBody RagDocumentRequest request,
            Authentication auth
    ) {
        Long companyId = getCompanyIdFromAuth(auth);
        return ragDocumentService.addTextDocument(companyId, request);
    }

    @PostMapping("/my-company/file")
    public CommonResponse uploadFile(
            @RequestParam(required = false) String title,
            @RequestPart("file") MultipartFile file,
            Authentication auth
    ) {
        Long companyId = getCompanyIdFromAuth(auth);
        return ragDocumentService.uploadFileDocument(companyId, title, file);
    }

    @GetMapping("/my-company")
    public PaginationResponse<RagDocumentResponse> getMyDocuments(
            @RequestParam(defaultValue = "0") Integer pageNumber,
            @RequestParam(defaultValue = "10") Integer pageSize,
            Authentication auth
    ) {
        Long companyId = getCompanyIdFromAuth(auth);
        return ragDocumentService.getDocumentsByCompany(companyId, pageNumber, pageSize);
    }
}
