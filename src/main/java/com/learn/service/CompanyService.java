package com.learn.service;

import com.learn.dto.CompanyRequest;
import com.learn.response.CompanyResponse;
import com.learn.response.CommonResponse;
import com.learn.response.DropdownResponse;
import com.learn.response.PaginationResponse;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface CompanyService {

    CommonResponse addCompany(CompanyRequest request);

    CommonResponse updateCompany(Long companyId,
                                 CompanyRequest request);

    CompanyResponse getCompany(Long companyId);

    List<CompanyResponse> getAllCompanies();

    PaginationResponse<CompanyResponse> getAllCompaniesWithPagination(
            Integer pageNumber,
            Integer pageSize
    );

    ResponseEntity<ByteArrayResource> downloadCompanyExcel();

    List<DropdownResponse> getCompanyDropdown();

}