package com.learn.serviceimpl;

import com.learn.dto.*;
import com.learn.entity.Company;
import com.learn.entity.User;
import com.learn.enums.UserRole;
import com.learn.enums.UserType;
import com.learn.repository.CompanyRepository;
import com.learn.repository.UserRepository;
import com.learn.response.CommonResponse;
import com.learn.response.LoginResponse;
import com.learn.security.JwtService;
import com.learn.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        String token = jwtService.generateToken(user);

        return LoginResponse.builder()
                .token(token)
                .message("Login successful")
                .build();
    }

    @Override
    public CommonResponse registerSuperAdmin(RegisterSuperAdminRequest request) {

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .mobileNumber(request.getMobileNumber())
                .userRole(UserRole.SUPER_ADMIN)
                .userType(UserType.ADMIN)
                .active(true)
                .build();

        userRepository.save(user);

        return CommonResponse.builder()
                .message("Super admin registered successfully")
                .build();
    }

    @Override
    public CommonResponse registerCompany(RegisterCompanyRequest request) {

        Company company = Company.builder()
                .companyName(request.getCompanyName())
                .companyEmail(request.getCompanyEmail())
                .companyContactNumber(request.getCompanyContactNumber())
                .companyAddress(request.getCompanyAddress())
                .active(true)
                .build();

        company = companyRepository.save(company);

        User companyAdmin = User.builder()
                .firstName(request.getAdminFirstName())
                .lastName(request.getAdminLastName())
                .email(request.getAdminEmail())
                .password(passwordEncoder.encode(request.getAdminPassword()))
                .mobileNumber(request.getAdminMobileNumber())
                .userRole(UserRole.COMPANY_ADMIN)
                .userType(UserType.COMPANY)
                .active(true)
                .company(company)
                .build();

        userRepository.save(companyAdmin);

        return CommonResponse.builder()
                .message("Company registered successfully")
                .build();
    }

    @Override
    public CommonResponse registerCompanyUser(RegisterCompanyUserRequest request) {

        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new RuntimeException("Company not found"));

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .mobileNumber(request.getMobileNumber())
                .userRole(UserRole.COMPANY_USER)
                .userType(UserType.COMPANY)
                .active(true)
                .company(company)
                .build();

        userRepository.save(user);

        return CommonResponse.builder()
                .message("Company user registered successfully")
                .build();
    }
}