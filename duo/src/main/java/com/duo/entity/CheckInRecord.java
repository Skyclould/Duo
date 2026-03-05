package com.duo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("check_in_record")
public class CheckInRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Integer durationMinutes; // Keeping it for compatibility or specific needs
    private String timeRange;      // "09:00 - 11:30"
    private String content;
    private String imageUrl;
    private String fileUrl;        // Document url
    private LocalDate checkInDate;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
