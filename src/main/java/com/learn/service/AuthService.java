package com.learn.service;

import com.learn.dto.*;
import com.learn.response.CommonResponse;
import com.learn.response.LoginResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    CommonResponse registerSuperAdmin(RegisterSuperAdminRequest request);

    CommonResponse registerCompany(RegisterCompanyRequest request);

    CommonResponse registerCompanyUser(RegisterCompanyUserRequest request);
}