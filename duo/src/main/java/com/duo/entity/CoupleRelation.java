package com.duo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("couple_relation")
public class CoupleRelation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userAId;
    private Long userBId;
    private Integer status; // 0-已解绑, 1-绑定中
    private String sharedGoal;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
