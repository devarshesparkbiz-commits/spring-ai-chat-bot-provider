package com.learn.serviceimpl;

import com.learn.dto.CompanyUserRequest;
import com.learn.entity.Company;
import com.learn.entity.User;
import com.learn.enums.UserRole;
import com.learn.enums.UserType;
import com.learn.repository.CompanyRepository;
import com.learn.repository.UserRepository;
import com.learn.response.CommonResponse;
import com.learn.response.CompanyUserResponse;
import com.learn.response.DropdownResponse;
import com.learn.response.PaginationResponse;
import com.learn.service.CompanyUserService;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CompanyUserServiceImpl
        implements CompanyUserService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;

    private CompanyUserResponse toResponse(User user) {
        return CompanyUserResponse.builder()
                .userId(user.getUserId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .mobileNumber(user.getMobileNumber())
                .companyName(user.getCompany() != null
                        ? user.getCompany().getCompanyName() : null)
                .companyId(user.getCompany() != null
                        ? user.getCompany().getCompanyId() : null)
                .userRole(user.getUserRole())
                .active(user.getActive())
                .build();
    }

    @Override
    public CommonResponse addCompanyUser(
            CompanyUserRequest request
    ) {

        Company company = companyRepository.findById(
                        request.getCompanyId()
                )
                .orElseThrow(() ->
                        new RuntimeException("Company not found"));

        // Default to COMPANY_USER if not specified
        UserRole role = request.getUserRole() != null
                ? request.getUserRole()
                : UserRole.COMPANY_USER;

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .mobileNumber(request.getMobileNumber())
                .userRole(role)
                .userType(UserType.COMPANY)
                .company(company)
                .active(request.getActive())
                .build();

        userRepository.save(user);

        return CommonResponse.builder()
                .message("Company user added successfully")
                .build();
    }

    @Override
    public CommonResponse updateCompanyUser(
            Long userId,
            CompanyUserRequest request
    ) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("Company user not found"));

        Company company = companyRepository.findById(
                        request.getCompanyId()
                )
                .orElseThrow(() ->
                        new RuntimeException("Company not found"));

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());

        if (request.getPassword() != null &&
                !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        user.setMobileNumber(request.getMobileNumber());
        user.setCompany(company);
        user.setActive(request.getActive());

        if (request.getUserRole() != null) {
            user.setUserRole(request.getUserRole());
        }

        userRepository.save(user);

        return CommonResponse.builder()
                .message("Company user updated successfully")
                .build();
    }

    @Override
    public CommonResponse softDeleteCompanyUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Company user not found"));
        user.setActive(false);
        userRepository.save(user);
        return CommonResponse.builder()
                .message("Company user deactivated successfully")
                .build();
    }

    @Override
    public CompanyUserResponse getCompanyUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("Company user not found"));
        return toResponse(user);
    }

    @Override
    public List<CompanyUserResponse> getAllCompanyUsers() {
        return userRepository
                .findByUserTypeAndUserRoleIn(
                        UserType.COMPANY,
                        List.of(UserRole.COMPANY_ADMIN, UserRole.COMPANY_USER)
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public PaginationResponse<CompanyUserResponse>
    getCompanyUsersWithPagination(Integer pageNumber, Integer pageSize) {

        Page<User> page = userRepository
                .findByUserTypeAndUserRoleIn(
                        UserType.COMPANY,
                        List.of(UserRole.COMPANY_ADMIN, UserRole.COMPANY_USER),
                        PageRequest.of(pageNumber, pageSize)
                );

        return PaginationResponse.<CompanyUserResponse>builder()
                .data(page.getContent().stream().map(this::toResponse).toList())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .lastPage(page.isLast())
                .build();
    }

    @Override
    public PaginationResponse<CompanyUserResponse>
    getCompanyUsersByCompanyWithPagination(
            Long companyId, Integer pageNumber, Integer pageSize
    ) {

        Page<User> page = userRepository
                .findByUserTypeAndUserRoleInAndCompany_CompanyId(
                        UserType.COMPANY,
                        List.of(UserRole.COMPANY_ADMIN, UserRole.COMPANY_USER),
                        companyId,
                        PageRequest.of(pageNumber, pageSize)
                );

        return PaginationResponse.<CompanyUserResponse>builder()
                .data(page.getContent().stream().map(this::toResponse).toList())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .lastPage(page.isLast())
                .build();
    }

    @Override
    public ResponseEntity<ByteArrayResource> downloadCompanyUsersExcel() {

        try {
            List<User> users = userRepository.findByUserTypeAndUserRoleIn(
                    UserType.COMPANY,
                    List.of(UserRole.COMPANY_ADMIN, UserRole.COMPANY_USER)
            );

            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet("Company Users");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("First Name");
            header.createCell(1).setCellValue("Last Name");
            header.createCell(2).setCellValue("Email");
            header.createCell(3).setCellValue("Mobile Number");
            header.createCell(4).setCellValue("Company");
            header.createCell(5).setCellValue("Role");
            header.createCell(6).setCellValue("Active");

            int rowNum = 1;
            for (User user : users) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(user.getFirstName());
                row.createCell(1).setCellValue(user.getLastName());
                row.createCell(2).setCellValue(user.getEmail());
                row.createCell(3).setCellValue(user.getMobileNumber());
                row.createCell(4).setCellValue(
                        user.getCompany() != null ? user.getCompany().getCompanyName() : "");
                row.createCell(5).setCellValue(
                        user.getUserRole() != null ? user.getUserRole().name() : "");
                row.createCell(6).setCellValue(user.getActive());
            }

            for (int i = 0; i < 7; i++) sheet.autoSizeColumn(i);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            workbook.close();

            ByteArrayResource resource = new ByteArrayResource(out.toByteArray());

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=company-users.xlsx")
                    .contentType(MediaType.parseMediaType(
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .contentLength(resource.contentLength())
                    .body(resource);

        } catch (Exception e) {
            throw new RuntimeException("Failed to download company users excel");
        }
    }

    @Override
    public List<DropdownResponse> getCompanyUserDropdown() {
        return userRepository
                .findByUserTypeAndUserRoleIn(
                        UserType.COMPANY,
                        List.of(UserRole.COMPANY_ADMIN, UserRole.COMPANY_USER)
                )
                .stream()
                .map(user -> DropdownResponse.builder()
                        .id(user.getUserId())
                        .name(user.getFirstName() + " " + user.getLastName())
                        .build())
                .toList();
    }
}