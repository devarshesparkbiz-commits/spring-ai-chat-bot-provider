package com.learn.response;

import com.learn.enums.UserRole;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyUserResponse {

    private Long userId;

    private String firstName;

    private String lastName;

    private String email;

    private String mobileNumber;

    private String companyName;

    private Long companyId;

    private UserRole userRole;

    private Boolean active;
}