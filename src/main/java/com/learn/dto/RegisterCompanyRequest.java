package com.learn.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterCompanyRequest {

    private String companyName;

    private String companyEmail;

    private String companyContactNumber;

    private String companyAddress;

    private String adminFirstName;

    private String adminLastName;

    private String adminEmail;

    private String adminPassword;

    private String adminMobileNumber;
}