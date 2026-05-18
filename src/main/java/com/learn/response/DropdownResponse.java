package com.learn.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DropdownResponse {

    private Long id;

    private String name;
}