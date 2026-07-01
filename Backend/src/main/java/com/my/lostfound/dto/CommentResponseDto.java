package com.my.lostfound.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentResponseDto {

    private Long id;
    private String content;
    private Long authorId;
    private String authorName;
    private LocalDateTime createdAt;
}
