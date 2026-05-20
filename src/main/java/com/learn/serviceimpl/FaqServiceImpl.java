package com.learn.serviceimpl;

import com.learn.dto.FaqRequest;
import com.learn.entity.Company;
import com.learn.entity.Faq;
import com.learn.repository.CompanyRepository;
import com.learn.repository.FaqRepository;
import com.learn.response.CommonResponse;
import com.learn.response.FaqResponse;
import com.learn.response.PaginationResponse;
import com.learn.service.FaqService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FaqServiceImpl implements FaqService {

    private final FaqRepository faqRepository;
    private final CompanyRepository companyRepository;

    private FaqResponse toResponse(Faq faq) {
        return FaqResponse.builder()
                .faqId(faq.getFaqId())
                .question(faq.getQuestion())
                .answer(faq.getAnswer())
                .active(faq.getActive())
                .companyId(faq.getCompany().getCompanyId())
                .companyName(faq.getCompany().getCompanyName())
                .createdAt(faq.getCreatedAt())
                .updatedAt(faq.getUpdatedAt())
                .build();
    }

    @Override
    public CommonResponse addFaq(FaqRequest request) {

        Company company = companyRepository
                .findById(request.getCompanyId())
                .orElseThrow(() ->
                        new RuntimeException("Company not found"));

        Faq faq = Faq.builder()
                .question(request.getQuestion())
                .answer(request.getAnswer())
                .active(request.getActive() != null
                        ? request.getActive()
                        : true)
                .company(company)
                .build();

        faqRepository.save(faq);

        return CommonResponse.builder()
                .message("FAQ added successfully")
                .build();
    }

    @Override
    public CommonResponse updateFaq(Long faqId, FaqRequest request) {

        Faq faq = faqRepository.findById(faqId)
                .orElseThrow(() ->
                        new RuntimeException("FAQ not found"));

        Company company = companyRepository
                .findById(request.getCompanyId())
                .orElseThrow(() ->
                        new RuntimeException("Company not found"));

        faq.setQuestion(request.getQuestion());
        faq.setAnswer(request.getAnswer());
        faq.setActive(request.getActive());
        faq.setCompany(company);

        faqRepository.save(faq);

        return CommonResponse.builder()
                .message("FAQ updated successfully")
                .build();
    }

    @Override
    public CommonResponse softDeleteFaq(Long faqId) {
        Faq faq = faqRepository.findById(faqId)
                .orElseThrow(() -> new RuntimeException("FAQ not found"));
        faq.setActive(false);
        faqRepository.save(faq);
        return CommonResponse.builder()
                .message("FAQ deactivated successfully")
                .build();
    }

    @Override
    public FaqResponse getFaq(Long faqId) {

        Faq faq = faqRepository.findById(faqId)
                .orElseThrow(() ->
                        new RuntimeException("FAQ not found"));

        return toResponse(faq);
    }

    @Override
    public List<FaqResponse> getFaqsByCompany(Long companyId) {

        return faqRepository
                .findByCompany_CompanyId(companyId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public PaginationResponse<FaqResponse> getFaqsByCompanyWithPagination(
            Long companyId,
            Integer pageNumber,
            Integer pageSize
    ) {

        Page<Faq> page = faqRepository.findByCompany_CompanyId(
                companyId,
                PageRequest.of(pageNumber, pageSize)
        );

        List<FaqResponse> faqs = page.getContent()
                .stream()
                .map(this::toResponse)
                .toList();

        return PaginationResponse.<FaqResponse>builder()
                .data(faqs)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .lastPage(page.isLast())
                .build();
    }
}
