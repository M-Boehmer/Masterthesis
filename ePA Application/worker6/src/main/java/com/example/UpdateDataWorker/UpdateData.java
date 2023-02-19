package com.example.UpdateDataWorker;

import io.camunda.zeebe.spring.client.EnableZeebeClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = {"com.example"})
@EnableZeebeClient
@SpringBootApplication(exclude={DataSourceAutoConfiguration.class})
public class UpdateData {
    public static void main(String[] args) {
        SpringApplication.run(UpdateData.class, args);
    }
}