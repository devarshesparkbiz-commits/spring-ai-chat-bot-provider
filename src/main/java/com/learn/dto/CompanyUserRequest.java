package com.learn.dto;

import com.learn.enums.UserRole;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyUserRequest {

    private String firstName;

    private String lastName;

    private String email;

    private String password;

    private String mobileNumber;

    private Long companyId;

    private Boolean active;

    /**
     * COMPANY_ADMIN → company admin user
     * COMPANY_USER  → regular company user (default)
     */
    private UserRole userRole;
}