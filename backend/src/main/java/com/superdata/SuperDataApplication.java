package com.superdata;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.superdata.mapper")
public class SuperDataApplication {

    public static void main(String[] args) {
        SpringApplication.run(SuperDataApplication.class, args);
    }
}
