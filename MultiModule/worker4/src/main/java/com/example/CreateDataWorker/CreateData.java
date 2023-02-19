package com.example.CreateDataWorker;

import io.camunda.zeebe.spring.client.EnableZeebeClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = {"com.example"})
@EnableZeebeClient
@SpringBootApplication(exclude={DataSourceAutoConfiguration.class})
public class CreateData {
    public static void main(String[] args) {
        SpringApplication.run(CreateData.class, args);
    }
}