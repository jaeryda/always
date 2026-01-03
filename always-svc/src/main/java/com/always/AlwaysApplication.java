package com.always;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.always.mapper")
public class AlwaysApplication {

    public static void main(String[] args) {
        SpringApplication.run(AlwaysApplication.class, args);
    }
}
