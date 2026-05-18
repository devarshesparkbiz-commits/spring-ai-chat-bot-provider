package com.learn.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserResponse {

    private Long userId;

    private String firstName;

    private String lastName;

    private String email;

    private String mobileNumber;

    private Boolean active;
}