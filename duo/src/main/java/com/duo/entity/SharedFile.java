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
    private Long fileSize; // for links this might be 0 or null
    private String category; // e.g. "考研资料", "复习笔记"
    private String type; // "FILE" or "LINK"
    private String description; // for links
    private LocalDateTime createTime;
}
