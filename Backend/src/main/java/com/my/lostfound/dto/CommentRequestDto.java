package com.my.lostfound.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentRequestDto {

    @NotBlank(message = "Content cannot be empty")
    @Size(max = 1000, message = "Comment is too long")
    private String content;

}
