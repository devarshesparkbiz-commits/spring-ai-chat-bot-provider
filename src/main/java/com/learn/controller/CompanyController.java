package com.learn.controller;

import com.learn.dto.CompanyRequest;
import com.learn.response.CompanyResponse;
import com.learn.response.CommonResponse;
import com.learn.response.DropdownResponse;
import com.learn.response.PaginationResponse;
import com.learn.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/company")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @PostMapping
    public CommonResponse addCompany(
            @RequestBody CompanyRequest request
    ) {
        return companyService.addCompany(request);
    }

    @PutMapping("/{companyId:\\d+}")
    public CommonResponse updateCompany(
            @PathVariable Long companyId,
            @RequestBody CompanyRequest request
    ) {
        return companyService.updateCompany(companyId, request);
    }

    @DeleteMapping("/{companyId:\\d+}")
    public CommonResponse softDeleteCompany(
            @PathVariable Long companyId
    ) {
        return companyService.softDeleteCompany(companyId);
    }

    @GetMapping("/{companyId:\\d+}")
    public CompanyResponse getCompany(
            @PathVariable Long companyId
    ) {
        return companyService.getCompany(companyId);
    }

    @GetMapping("/list")
    public List<CompanyResponse> getAllCompanies() {
        return companyService.getAllCompanies();
    }

    @GetMapping("/pagination")
    public PaginationResponse<CompanyResponse>
    getAllCompaniesWithPagination(

            @RequestParam(defaultValue = "0")
            Integer pageNumber,

            @RequestParam(defaultValue = "10")
            Integer pageSize
    ) {

        return companyService
                .getAllCompaniesWithPagination(
                        pageNumber,
                        pageSize
                );
    }

    @GetMapping("/download/excel")
    public ResponseEntity<ByteArrayResource>
    downloadCompanyExcel() {

        return companyService.downloadCompanyExcel();
    }

    @GetMapping("/dropdown")
    public List<DropdownResponse>
    getCompanyDropdown() {

        return companyService
                .getCompanyDropdown();
    }
}