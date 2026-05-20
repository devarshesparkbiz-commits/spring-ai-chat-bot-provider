package com.learn.service;

import com.learn.dto.AdminUserRequest;
import com.learn.response.AdminUserResponse;
import com.learn.response.CommonResponse;
import com.learn.response.DropdownResponse;
import com.learn.response.PaginationResponse;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface AdminUserService {

    CommonResponse addAdminUser(
            AdminUserRequest request
    );

    CommonResponse updateAdminUser(
            Long userId,
            AdminUserRequest request
    );

    CommonResponse softDeleteAdminUser(Long userId);

    AdminUserResponse getAdminUser(
            Long userId
    );

    List<AdminUserResponse> getAllAdminUsers();

    PaginationResponse<AdminUserResponse>
    getAdminUsersWithPagination(
            Integer pageNumber,
            Integer pageSize
    );

    ResponseEntity<ByteArrayResource>
    downloadAdminUsersExcel();

    List<DropdownResponse> getAdminUserDropdown();
}