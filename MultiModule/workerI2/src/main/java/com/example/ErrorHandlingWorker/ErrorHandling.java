package com.example.ErrorHandlingWorker;

import io.camunda.zeebe.spring.client.EnableZeebeClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = {"com.example"})
@EnableZeebeClient
@SpringBootApplication(exclude={DataSourceAutoConfiguration.class})
public class ErrorHandling {
    public static void main(String[] args) {
        SpringApplication.run(ErrorHandling.class, args);
    }
}