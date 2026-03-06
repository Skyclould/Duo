package com.duo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseConfigRunner implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        try {
            jdbcTemplate.execute("ALTER TABLE check_in_record ADD COLUMN time_range VARCHAR(50) DEFAULT NULL COMMENT '打卡时间段'");
        } catch (Exception e) {
            // Column might already exist
        }
        try {
            jdbcTemplate.execute("ALTER TABLE check_in_record ADD COLUMN file_url VARCHAR(255) DEFAULT NULL COMMENT '附件文档URL'");
        } catch (Exception e) {
            // Column might already exist
        }
        try {
            jdbcTemplate.execute("ALTER TABLE learning_plan ADD COLUMN deadline DATETIME DEFAULT NULL COMMENT '截止日期'");
        } catch (Exception e) {
            // Column might already exist
        }
        
        try {
            jdbcTemplate.execute("ALTER TABLE shared_file ADD COLUMN category VARCHAR(100) DEFAULT '默认分类' COMMENT '分类目录'");
        } catch (Exception e) {}
        try {
            jdbcTemplate.execute("ALTER TABLE shared_file ADD COLUMN type VARCHAR(20) DEFAULT 'FILE' COMMENT '类型：FILE/LINK'");
        } catch (Exception e) {}
        try {
            jdbcTemplate.execute("ALTER TABLE shared_file ADD COLUMN description VARCHAR(500) DEFAULT NULL COMMENT '链接描述/推荐语'");
        } catch (Exception e) {}
    }
}
