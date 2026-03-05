package com.duo.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class LearningPlanDTO {
    private String content;
    private LocalDateTime deadline;
}
