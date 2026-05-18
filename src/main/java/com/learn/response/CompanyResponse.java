package com.learn.response;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyResponse {

    private Long companyId;

    private String companyName;

    private String companyEmail;

    private String companyContactNumber;

    private String companyAddress;

    private Boolean active;
}