package com.duo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.duo.mapper")
public class DuoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DuoApplication.class, args);
    }
}
