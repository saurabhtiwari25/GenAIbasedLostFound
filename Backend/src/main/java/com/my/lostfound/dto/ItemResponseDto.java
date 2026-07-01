package com.my.lostfound.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemResponseDto {

    private Long id;

    private String title;

    private String description;

    private String location;

    private boolean found;

    private String imagePath;

    private String contactInfo;

    private String foundLocation;

    private String founderName;

    private String founderContactInfo;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt; 

    private Long reporterId;

    private String reporterName;
}