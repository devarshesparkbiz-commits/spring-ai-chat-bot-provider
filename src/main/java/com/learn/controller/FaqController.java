package com.learn.controller;

import com.learn.dto.FaqRequest;
import com.learn.response.CommonResponse;
import com.learn.response.FaqResponse;
import com.learn.response.PaginationResponse;
import com.learn.service.FaqService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/faq")
@RequiredArgsConstructor
public class FaqController {

    private final FaqService faqService;

    // ── Helper: extract companyId from JWT claims stored in auth details ──────
    private Long getCompanyIdFromAuth(Authentication auth) {
        if (auth.getDetails() instanceof Claims claims) {
            Object raw = claims.get("companyId");
            if (raw == null) {
                throw new RuntimeException("No companyId in token");
            }
            return ((Number) raw).longValue();
        }
        throw new RuntimeException("Unable to resolve companyId from token");
    }

    // ── SUPER_ADMIN endpoints ─────────────────────────────────────────────────

    @PostMapping
    public CommonResponse addFaq(
            @RequestBody FaqRequest request
    ) {
        return faqService.addFaq(request);
    }

    @PutMapping("/{faqId:\\d+}")
    public CommonResponse updateFaq(
            @PathVariable Long faqId,
            @RequestBody FaqRequest request
    ) {
        return faqService.updateFaq(faqId, request);
    }

    @GetMapping("/{faqId:\\d+}")
    public FaqResponse getFaq(
            @PathVariable Long faqId
    ) {
        return faqService.getFaq(faqId);
    }

    @DeleteMapping("/{faqId:\\d+}")
    public CommonResponse softDeleteFaq(
            @PathVariable Long faqId
    ) {
        return faqService.softDeleteFaq(faqId);
    }

    @GetMapping("/company/{companyId:\\d+}")
    public List<FaqResponse> getFaqsByCompany(
            @PathVariable Long companyId
    ) {
        return faqService.getFaqsByCompany(companyId);
    }

    @GetMapping("/company/{companyId:\\d+}/pagination")
    public PaginationResponse<FaqResponse> getFaqsByCompanyWithPagination(
            @PathVariable Long companyId,

            @RequestParam(defaultValue = "0")
            Integer pageNumber,

            @RequestParam(defaultValue = "10")
            Integer pageSize
    ) {
        return faqService.getFaqsByCompanyWithPagination(
                companyId,
                pageNumber,
                pageSize
        );
    }

    // ── COMPANY_ADMIN endpoints (companyId resolved from JWT) ─────────────────

    @PostMapping("/my-company")
    public CommonResponse addFaqForMyCompany(
            @RequestBody FaqRequest request,
            Authentication auth
    ) {
        Long companyId = getCompanyIdFromAuth(auth);
        request.setCompanyId(companyId);
        return faqService.addFaq(request);
    }

    @PutMapping("/my-company/{faqId:\\d+}")
    public CommonResponse updateFaqForMyCompany(
            @PathVariable Long faqId,
            @RequestBody FaqRequest request,
            Authentication auth
    ) {
        Long companyId = getCompanyIdFromAuth(auth);
        request.setCompanyId(companyId);
        return faqService.updateFaq(faqId, request);
    }

    @GetMapping("/my-company")
    public List<FaqResponse> getFaqsForMyCompany(
            Authentication auth
    ) {
        Long companyId = getCompanyIdFromAuth(auth);
        return faqService.getFaqsByCompany(companyId);
    }

    @GetMapping("/my-company/pagination")
    public PaginationResponse<FaqResponse> getFaqsForMyCompanyWithPagination(
            @RequestParam(defaultValue = "0") Integer pageNumber,
            @RequestParam(defaultValue = "10") Integer pageSize,
            Authentication auth
    ) {
        Long companyId = getCompanyIdFromAuth(auth);
        return faqService.getFaqsByCompanyWithPagination(companyId, pageNumber, pageSize);
    }
}
