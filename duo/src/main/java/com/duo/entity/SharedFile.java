package com.duo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("shared_file")
public class SharedFile {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long uploaderId;
    private Long partnerId;
    private String fileName;
    private String fileUrl;
    private Long fileSize;
    private LocalDateTime createTime;
}
