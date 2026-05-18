package com.learn.service;

import com.learn.dto.CompanyUserRequest;
import com.learn.response.CommonResponse;
import com.learn.response.CompanyUserResponse;
import com.learn.response.DropdownResponse;
import com.learn.response.PaginationResponse;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface CompanyUserService {

    CommonResponse addCompanyUser(
            CompanyUserRequest request
    );

    CommonResponse updateCompanyUser(
            Long userId,
            CompanyUserRequest request
    );

    CompanyUserResponse getCompanyUser(
            Long userId
    );

    List<CompanyUserResponse> getAllCompanyUsers();

    PaginationResponse<CompanyUserResponse>
    getCompanyUsersWithPagination(
            Integer pageNumber,
            Integer pageSize
    );

    PaginationResponse<CompanyUserResponse>
    getCompanyUsersByCompanyWithPagination(
            Long companyId,
            Integer pageNumber,
            Integer pageSize
    );

    ResponseEntity<ByteArrayResource>
    downloadCompanyUsersExcel();

    List<DropdownResponse>
    getCompanyUserDropdown();
}