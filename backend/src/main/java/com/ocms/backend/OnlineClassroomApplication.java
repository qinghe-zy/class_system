package com.ocms.backend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.ocms.backend.mapper")
public class OnlineClassroomApplication {

    public static void main(String[] args) {
        SpringApplication.run(OnlineClassroomApplication.class, args);
    }
}
