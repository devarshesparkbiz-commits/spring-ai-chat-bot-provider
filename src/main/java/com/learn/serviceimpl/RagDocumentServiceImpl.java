package com.learn.serviceimpl;

import com.learn.dto.RagDocumentRequest;
import com.learn.entity.Company;
import com.learn.entity.RagDocument;
import com.learn.enums.RagDocumentType;
import com.learn.repository.CompanyRepository;
import com.learn.repository.RagDocumentRepository;
import com.learn.response.CommonResponse;
import com.learn.response.PaginationResponse;
import com.learn.response.RagDocumentResponse;
import com.learn.service.RagDocumentService;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RagDocumentServiceImpl implements RagDocumentService {

    private final RagDocumentRepository ragDocumentRepository;
    private final CompanyRepository     companyRepository;

    // ── Mapper ────────────────────────────────────────────────────────────────

    private RagDocumentResponse toResponse(RagDocument doc) {
        return RagDocumentResponse.builder()
                .documentId(doc.getDocumentId())
                .documentTitle(doc.getDocumentTitle())
                .content(doc.getContent())
                .documentType(doc.getDocumentType())
                .active(doc.getActive())
                .companyId(doc.getCompany().getCompanyId())
                .companyName(doc.getCompany().getCompanyName())
                .createdAt(doc.getCreatedAt())
                .updatedAt(doc.getUpdatedAt())
                .build();
    }

    // ── Text document ─────────────────────────────────────────────────────────

    @Override
    public CommonResponse addTextDocument(Long companyId, RagDocumentRequest request) {

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        RagDocument doc = RagDocument.builder()
                .documentTitle(request.getDocumentTitle())
                .content(request.getContent())
                .documentType(RagDocumentType.TEXT)
                .active(request.getActive() != null ? request.getActive() : true)
                .company(company)
                .build();

        ragDocumentRepository.save(doc);

        return CommonResponse.builder()
                .message("RAG document added successfully")
                .build();
    }

    // ── File upload ───────────────────────────────────────────────────────────

    @Override
    public CommonResponse uploadFileDocument(Long companyId,
                                             String title,
                                             MultipartFile file) {

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        String extractedText = extractText(file);

        RagDocument doc = RagDocument.builder()
                .documentTitle(title != null && !title.isBlank()
                        ? title
                        : file.getOriginalFilename())
                .content(extractedText)
                .documentType(RagDocumentType.FILE)
                .active(true)
                .company(company)
                .build();

        ragDocumentRepository.save(doc);

        return CommonResponse.builder()
                .message("File uploaded and indexed successfully")
                .build();
    }

    /**
     * Extracts plain text from supported file types.
     * Supported: .txt, .docx
     * Unsupported formats are rejected with a clear error rather than
     * attempting a raw binary read (which produces null bytes that
     * PostgreSQL rejects with "invalid byte sequence for encoding UTF8: 0x00").
     */
    private String extractText(MultipartFile file) {
        String filename = file.getOriginalFilename() != null
                ? file.getOriginalFilename().toLowerCase()
                : "";

        try {
            String raw;

            if (filename.endsWith(".txt")) {
                raw = new BufferedReader(
                        new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))
                        .lines()
                        .collect(Collectors.joining("\n"));

            } else if (filename.endsWith(".docx")) {
                try (XWPFDocument docx = new XWPFDocument(file.getInputStream())) {
                    raw = docx.getParagraphs()
                            .stream()
                            .map(XWPFParagraph::getText)
                            .filter(t -> t != null && !t.isBlank())
                            .collect(Collectors.joining("\n"));
                }

            } else if (filename.endsWith(".pdf")) {
                try (PDDocument pdf = Loader.loadPDF(file.getBytes())) {
                    raw = new PDFTextStripper().getText(pdf);
                }

            } else {
                // Reject binary formats (PDF, XLS, images, etc.) explicitly.
                // Reading them as UTF-8 text produces null bytes (0x00) that
                // PostgreSQL refuses to store in TEXT columns.
                throw new RuntimeException(
                        "Unsupported file type: '" + filename + "'. " +
                        "Please upload a .txt, .docx or .pdf file.");
            }

            // Strip null bytes — PostgreSQL TEXT columns cannot store 0x00
            // regardless of how they got into the string.
            return raw.replace("\u0000", "");

        } catch (RuntimeException e) {
            throw e;
        } catch (IOException e) {
            throw new RuntimeException("Failed to extract text from file: " + e.getMessage(), e);
        }
    }

    // ── CRUD ──────────────────────────────────────────────────────────────────

    @Override
    public CommonResponse updateDocument(Long documentId, RagDocumentRequest request) {

        RagDocument doc = ragDocumentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        if (request.getDocumentTitle() != null) {
            doc.setDocumentTitle(request.getDocumentTitle());
        }
        if (request.getContent() != null) {
            doc.setContent(request.getContent());
        }
        if (request.getActive() != null) {
            doc.setActive(request.getActive());
        }

        ragDocumentRepository.save(doc);

        return CommonResponse.builder()
                .message("Document updated successfully")
                .build();
    }

    @Override
    public CommonResponse deleteDocument(Long documentId) {

        RagDocument doc = ragDocumentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        doc.setActive(false);
        ragDocumentRepository.save(doc);

        return CommonResponse.builder()
                .message("Document removed successfully")
                .build();
    }

    @Override
    public PaginationResponse<RagDocumentResponse> getDocumentsByCompany(
            Long companyId, Integer pageNumber, Integer pageSize) {

        Page<RagDocument> page = ragDocumentRepository
                .findByCompany_CompanyId(companyId, PageRequest.of(pageNumber, pageSize));

        List<RagDocumentResponse> data = page.getContent()
                .stream()
                .map(this::toResponse)
                .toList();

        return PaginationResponse.<RagDocumentResponse>builder()
                .data(data)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .lastPage(page.isLast())
                .build();
    }

    @Override
    public RagDocumentResponse getDocument(Long documentId) {
        RagDocument doc = ragDocumentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));
        return toResponse(doc);
    }
}
