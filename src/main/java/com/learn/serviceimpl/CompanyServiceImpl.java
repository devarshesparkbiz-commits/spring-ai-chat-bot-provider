package com.learn.serviceimpl;

import com.learn.dto.CompanyRequest;
import com.learn.entity.Company;
import com.learn.repository.CompanyRepository;
import com.learn.response.CompanyResponse;
import com.learn.response.CommonResponse;
import com.learn.response.DropdownResponse;
import com.learn.response.PaginationResponse;
import com.learn.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;

    @Override
    public CommonResponse addCompany(CompanyRequest request) {

        Company company = Company.builder()
                .companyName(request.getCompanyName())
                .companyEmail(request.getCompanyEmail())
                .companyContactNumber(request.getCompanyContactNumber())
                .companyAddress(request.getCompanyAddress())
                .active(request.getActive())
                .build();

        companyRepository.save(company);

        return CommonResponse.builder()
                .message("Company added successfully")
                .build();
    }

    @Override
    public CommonResponse updateCompany(Long companyId,
                                        CompanyRequest request) {

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() ->
                        new RuntimeException("Company not found"));

        company.setCompanyName(request.getCompanyName());
        company.setCompanyEmail(request.getCompanyEmail());
        company.setCompanyContactNumber(request.getCompanyContactNumber());
        company.setCompanyAddress(request.getCompanyAddress());
        company.setActive(request.getActive());

        companyRepository.save(company);

        return CommonResponse.builder()
                .message("Company updated successfully")
                .build();
    }

    @Override
    public CommonResponse softDeleteCompany(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));
        company.setActive(false);
        companyRepository.save(company);
        return CommonResponse.builder()
                .message("Company deactivated successfully")
                .build();
    }

    @Override
    public CompanyResponse getCompany(Long companyId) {

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() ->
                        new RuntimeException("Company not found"));

        return CompanyResponse.builder()
                .companyId(company.getCompanyId())
                .companyName(company.getCompanyName())
                .companyEmail(company.getCompanyEmail())
                .companyContactNumber(company.getCompanyContactNumber())
                .companyAddress(company.getCompanyAddress())
                .active(company.getActive())
                .build();
    }

    @Override
    public List<CompanyResponse> getAllCompanies() {

        return companyRepository.findAll()
                .stream()
                .map(company -> CompanyResponse.builder()
                        .companyId(company.getCompanyId())
                        .companyName(company.getCompanyName())
                        .companyEmail(company.getCompanyEmail())
                        .companyContactNumber(company.getCompanyContactNumber())
                        .companyAddress(company.getCompanyAddress())
                        .active(company.getActive())
                        .build())
                .toList();
    }

    @Override
    public PaginationResponse<CompanyResponse>
    getAllCompaniesWithPagination(Integer pageNumber,
                                  Integer pageSize) {

        Page<Company> page = companyRepository.findAll(
                PageRequest.of(pageNumber, pageSize)
        );

        List<CompanyResponse> companies = page.getContent()
                .stream()
                .map(company -> CompanyResponse.builder()
                        .companyId(company.getCompanyId())
                        .companyName(company.getCompanyName())
                        .companyEmail(company.getCompanyEmail())
                        .companyContactNumber(company.getCompanyContactNumber())
                        .companyAddress(company.getCompanyAddress())
                        .active(company.getActive())
                        .build())
                .toList();

        return PaginationResponse.<CompanyResponse>builder()
                .data(companies)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .lastPage(page.isLast())
                .build();
    }

    @Override
    public ResponseEntity<ByteArrayResource> downloadCompanyExcel() {

        try {

            List<Company> companies = companyRepository.findAll();

            XSSFWorkbook workbook = new XSSFWorkbook();

            XSSFSheet sheet = workbook.createSheet("Companies");

            Row header = sheet.createRow(0);

//            header.createCell(0).setCellValue("Company ID");
            header.createCell(0).setCellValue("Company Name");
            header.createCell(1).setCellValue("Email");
            header.createCell(2).setCellValue("Contact Number");
            header.createCell(3).setCellValue("Address");
            header.createCell(4).setCellValue("Active");

            int rowNum = 1;

            for (Company company : companies) {

                Row row = sheet.createRow(rowNum++);

//                row.createCell(0).setCellValue(company.getCompanyId());
                row.createCell(0).setCellValue(company.getCompanyName());
                row.createCell(1).setCellValue(company.getCompanyEmail());
                row.createCell(2).setCellValue(company.getCompanyContactNumber());
                row.createCell(3).setCellValue(company.getCompanyAddress());
                row.createCell(4).setCellValue(company.getActive());
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();

            workbook.write(out);

            workbook.close();

            ByteArrayResource resource =
                    new ByteArrayResource(out.toByteArray());

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=companies.xlsx")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(resource.contentLength())
                    .body(resource);

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to download excel"
            );
        }
    }

    @Override
    public List<DropdownResponse>
    getCompanyDropdown() {

        return companyRepository.findAll()
                .stream()
                .map(company ->
                        DropdownResponse.builder()
                                .id(company.getCompanyId())
                                .name(company.getCompanyName())
                                .build()
                )
                .toList();
    }
}