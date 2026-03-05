-- 创建数据库
CREATE DATABASE IF NOT EXISTS `duo_study` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `duo_study`;

-- 1. 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '登录账号',
    `password` VARCHAR(100) NOT NULL COMMENT '密码',
    `nickname` VARCHAR(50) NOT NULL COMMENT '用户昵称',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `invite_code` VARCHAR(20) DEFAULT NULL COMMENT '专属邀请码',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_invite_code` (`invite_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2. 搭子关系表
CREATE TABLE IF NOT EXISTS `couple_relation` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '关系ID',
    `user_a_id` BIGINT NOT NULL COMMENT '用户A的ID',
    `user_b_id` BIGINT NOT NULL COMMENT '用户B的ID',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '关系状态: 0-已解绑, 1-绑定中',
    `shared_goal` VARCHAR(255) DEFAULT NULL COMMENT '共同目标',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '绑定时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_users` (`user_a_id`, `user_b_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='搭子关系表';

-- 3. 学习计划/任务表
CREATE TABLE IF NOT EXISTS `learning_plan` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '计划ID',
    `user_id` BIGINT NOT NULL COMMENT '所属用户ID',
    `content` VARCHAR(255) NOT NULL COMMENT '任务/计划内容',
    `deadline` DATETIME DEFAULT NULL COMMENT '截止时间',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0-未完成, 1-已完成',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习计划任务表';

-- 4. 学习打卡记录表
CREATE TABLE IF NOT EXISTS `check_in_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `duration_minutes` INT NOT NULL DEFAULT 0 COMMENT '学习时长（分钟）',
    `content` TEXT COMMENT '打卡总结/心得',
    `image_url` VARCHAR(255) DEFAULT NULL COMMENT '打卡图片URL',
    `check_in_date` DATE NOT NULL COMMENT '打卡日期',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '打卡时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_date` (`user_id`, `check_in_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='打卡记录表';

-- 5. 心情留言板表
CREATE TABLE IF NOT EXISTS `message` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '留言ID',
    `sender_id` BIGINT NOT NULL COMMENT '发送者ID',
    `receiver_id` BIGINT NOT NULL COMMENT '接收者ID',
    `content` VARCHAR(500) NOT NULL COMMENT '留言内容',
    `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读: 0-未读, 1-已读',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    PRIMARY KEY (`id`),
    KEY `idx_receiver` (`receiver_id`, `is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='心情留言表';

-- 6. 共享文件表
CREATE TABLE IF NOT EXISTS `shared_file` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '文件ID',
    `uploader_id` BIGINT NOT NULL COMMENT '上传者ID',
    `partner_id` BIGINT NOT NULL COMMENT '对方ID',
    `file_name` VARCHAR(255) NOT NULL COMMENT '原始文件名',
    `file_url` VARCHAR(500) NOT NULL COMMENT '文件下载/访问URL',
    `file_size` BIGINT DEFAULT 0 COMMENT '文件大小(字节)',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    PRIMARY KEY (`id`),
    KEY `idx_couple` (`uploader_id`, `partner_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='共享文件表';

-- 7. 个人笔记表
CREATE TABLE IF NOT EXISTS `note` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '笔记ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `title` VARCHAR(200) DEFAULT '我的笔记' COMMENT '笔记标题',
    `content` LONGTEXT COMMENT '笔记内容(Markdown)',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='个人笔记表';

