package com.example.model;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * 模型图服务启动类
 */
@SpringBootApplication
@EnableKafka
public class ModelApplication {

    public static void main(String[] args) {
        SpringApplication.run(ModelApplication.class, args);
        System.out.println("模型图服务启动成功！");
    }
}