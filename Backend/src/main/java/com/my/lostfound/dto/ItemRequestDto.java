package com.my.lostfound.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemRequestDto {

    @NotBlank(message = "Title cannot be empty")
    @Size(min = 2, max = 100, message = "Title must be 3 to 100 characters")
    private String title;

    @NotBlank(message = "Description cannot be empty")
    @Size(min = 5, max = 500, message = "Description must be 5 to 500 characters")
    private String description;

    @NotBlank(message = "Location cannot be empty")
    private String location;

    private boolean found;

    @NotBlank(message = "Contact info cannot be empty")
    private String contactInfo;

    @jakarta.validation.constraints.NotNull(message = "Reporter ID cannot be null")
    private Long reporterId;

}