package com.my.lostfound.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarkFoundRequestDto {

    @NotNull(message = "Finder user ID cannot be null")
    private Long finderUserId;

    @NotBlank(message = "Found location cannot be empty")
    private String foundLocation;

    @NotBlank(message = "Founder name cannot be empty")
    private String founderName;

    @NotBlank(message = "Founder contact info cannot be empty")
    private String founderContactInfo;
}
