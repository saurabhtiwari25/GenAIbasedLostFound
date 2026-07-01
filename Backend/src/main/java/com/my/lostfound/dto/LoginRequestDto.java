package com.my.lostfound.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequestDto {

    @NotBlank(message = "Username cannot be empty")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password cannot be empty")
    private String password;

}