package com.learn.controller;

import com.learn.dto.*;
import com.learn.response.CommonResponse;
import com.learn.response.LoginResponse;
import com.learn.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/register/super-admin")
    public CommonResponse registerSuperAdmin(
            @RequestBody RegisterSuperAdminRequest request
    ) {
        return authService.registerSuperAdmin(request);
    }

    @PostMapping("/register/company")
    public CommonResponse registerCompany(
            @RequestBody RegisterCompanyRequest request
    ) {
        return authService.registerCompany(request);
    }

    @PostMapping("/register/company-user")
    public CommonResponse registerCompanyUser(
            @RequestBody RegisterCompanyUserRequest request
    ) {
        return authService.registerCompanyUser(request);
    }
}