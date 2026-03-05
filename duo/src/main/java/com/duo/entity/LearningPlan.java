package com.duo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("learning_plan")
public class LearningPlan {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String content;
    private LocalDateTime deadline;
    private Integer status; // 0-未完成, 1-已完成
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
