package com.mikelekan.artgallery.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class ArtworkResponse
{
    private Long id;
    private String title;
    private String description;
    private String artist;
    private String imageUrl; // This will hold the Pre-signed URL
    private BigDecimal price;
    private boolean sold;
    private LocalDateTime createdAt;
}
