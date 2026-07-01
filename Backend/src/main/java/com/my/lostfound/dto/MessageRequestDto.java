package com.my.lostfound.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageRequestDto {

    @NotNull(message = "Receiver ID is required")
    private Long receiverId;

    @NotNull(message = "Item ID is required")
    private Long itemId;

    @NotBlank(message = "Content is required")
    private String content;
}
