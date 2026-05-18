package com.learn.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyRequest {

    private String companyName;

    private String companyEmail;

    private String companyContactNumber;

    private String companyAddress;

    private Boolean active;
}