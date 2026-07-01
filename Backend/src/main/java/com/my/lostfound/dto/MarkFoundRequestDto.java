package com.my.lostfound.dto;

import jakarta.validation.constraints.NotBlank;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarkFoundRequestDto {

    @NotBlank(message = "Found location cannot be empty")
    private String foundLocation;

    @NotBlank(message = "Founder name cannot be empty")
    private String founderName;

    @NotBlank(message = "Founder contact info cannot be empty")
    private String founderContactInfo;
}
