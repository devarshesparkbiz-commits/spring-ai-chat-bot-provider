package com.learn.controller;

import com.learn.dto.AdminUserRequest;
import com.learn.response.AdminUserResponse;
import com.learn.response.CommonResponse;
import com.learn.response.DropdownResponse;
import com.learn.response.PaginationResponse;
import com.learn.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin-user")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @PostMapping
    public CommonResponse addAdminUser(
            @RequestBody AdminUserRequest request
    ) {

        return adminUserService
                .addAdminUser(request);
    }

    @PutMapping("/{userId:\\d+}")
    public CommonResponse updateAdminUser(
            @PathVariable Long userId,
            @RequestBody AdminUserRequest request
    ) {

        return adminUserService
                .updateAdminUser(userId, request);
    }

    @GetMapping("/{userId:\\d+}")
    public AdminUserResponse getAdminUser(
            @PathVariable Long userId
    ) {

        return adminUserService
                .getAdminUser(userId);
    }

    @GetMapping("/list")
    public List<AdminUserResponse>
    getAllAdminUsers() {

        return adminUserService
                .getAllAdminUsers();
    }

    @GetMapping("/pagination")
    public PaginationResponse<AdminUserResponse>
    getAdminUsersWithPagination(

            @RequestParam(defaultValue = "0")
            Integer pageNumber,

            @RequestParam(defaultValue = "10")
            Integer pageSize
    ) {

        return adminUserService
                .getAdminUsersWithPagination(
                        pageNumber,
                        pageSize
                );
    }

    @GetMapping("/download/excel")
    public ResponseEntity<ByteArrayResource>
    downloadAdminUsersExcel() {

        return adminUserService
                .downloadAdminUsersExcel();
    }

    @GetMapping("/dropdown")
    public List<DropdownResponse>
    getAdminUserDropdown() {

        return adminUserService
                .getAdminUserDropdown();
    }
}