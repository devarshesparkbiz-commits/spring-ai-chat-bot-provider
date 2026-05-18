package com.learn.controller;

import com.learn.dto.CompanyUserRequest;
import com.learn.response.CommonResponse;
import com.learn.response.CompanyUserResponse;
import com.learn.response.DropdownResponse;
import com.learn.response.PaginationResponse;
import com.learn.service.CompanyUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/company-user")
@RequiredArgsConstructor
public class CompanyUserController {

    private final CompanyUserService companyUserService;

    @PostMapping
    public CommonResponse addCompanyUser(
            @RequestBody CompanyUserRequest request
    ) {

        return companyUserService
                .addCompanyUser(request);
    }

    @PutMapping("/{userId:\\d+}")
    public CommonResponse updateCompanyUser(
            @PathVariable Long userId,
            @RequestBody CompanyUserRequest request
    ) {

        return companyUserService
                .updateCompanyUser(userId, request);
    }

    @GetMapping("/{userId:\\d+}")
    public CompanyUserResponse getCompanyUser(
            @PathVariable Long userId
    ) {

        return companyUserService
                .getCompanyUser(userId);
    }

    @GetMapping("/list")
    public List<CompanyUserResponse>
    getAllCompanyUsers() {

        return companyUserService
                .getAllCompanyUsers();
    }

    @GetMapping("/pagination")
    public PaginationResponse<CompanyUserResponse>
    getCompanyUsersWithPagination(

            @RequestParam(defaultValue = "0")
            Integer pageNumber,

            @RequestParam(defaultValue = "10")
            Integer pageSize,

            @RequestParam(required = false)
            Long companyId
    ) {

        if (companyId != null) {
            return companyUserService
                    .getCompanyUsersByCompanyWithPagination(
                            companyId,
                            pageNumber,
                            pageSize
                    );
        }

        return companyUserService
                .getCompanyUsersWithPagination(
                        pageNumber,
                        pageSize
                );
    }

    @GetMapping("/download/excel")
    public ResponseEntity<ByteArrayResource>
    downloadCompanyUsersExcel() {

        return companyUserService
                .downloadCompanyUsersExcel();
    }

    @GetMapping("/dropdown")
    public List<DropdownResponse>
    getCompanyUserDropdown() {

        return companyUserService
                .getCompanyUserDropdown();
    }
}