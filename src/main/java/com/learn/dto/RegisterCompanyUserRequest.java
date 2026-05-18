package com.learn.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterCompanyUserRequest {

    private String firstName;

    private String lastName;

    private String email;

    private String password;

    private String mobileNumber;

    private Long companyId;
}