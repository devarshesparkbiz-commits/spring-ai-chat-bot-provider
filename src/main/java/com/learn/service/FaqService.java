package com.learn.service;

import com.learn.dto.FaqRequest;
import com.learn.response.CommonResponse;
import com.learn.response.FaqResponse;
import com.learn.response.PaginationResponse;

import java.util.List;

public interface FaqService {

    CommonResponse addFaq(FaqRequest request);

    CommonResponse updateFaq(Long faqId, FaqRequest request);

    CommonResponse softDeleteFaq(Long faqId);

    FaqResponse getFaq(Long faqId);

    List<FaqResponse> getFaqsByCompany(Long companyId);

    PaginationResponse<FaqResponse> getFaqsByCompanyWithPagination(
            Long companyId,
            Integer pageNumber,
            Integer pageSize
    );
}
