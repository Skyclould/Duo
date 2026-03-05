package com.duo.dto;

import lombok.Data;

@Data
public class CheckInDTO {
    private Integer durationMinutes;
    private String timeRange;
    private String content;
    private String imageUrl; // Optional for now
    private String fileUrl;
    private String checkInDate; // format: yyyy-MM-dd
}
