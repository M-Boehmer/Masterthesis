package com.example.worker6;

import io.camunda.zeebe.spring.client.EnableZeebeClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@ComponentScan(basePackages = {"com.example"})
@EnableZeebeClient
@EnableScheduling
@SpringBootApplication(exclude={DataSourceAutoConfiguration.class})
public class Frontend {
    public static void main(String[] args) {
        SpringApplication.run(Frontend.class, args);
    }
}