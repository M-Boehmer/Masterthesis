package com.example.core.config;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaTopicConfig {

    @Value(value = "${spring.kafka.bootstrap-servers}")
    private String bootstrapAddress;

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configprops = new HashMap<>();
        configprops.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        //configprops.put("sasl.mechanism","SCRAM-SHA-512");
        //configprops.put("security.protocol", "SASL_SSL");
        //configprops.put("sasl.jaas.config","org.apache.kafka.common.security.scram.ScramLoginModule required username='kafka' password='kafka' awsDebugCreds=true;");

        //configprops.put("sasl.mechanism","AWS_MSK_IAM");
        //configprops.put("security.protocol", "SASL_SSL");
        //configprops.put("sasl.jaas.config","software.amazon.msk.auth.iam.IAMLoginModule required ;");
        //configprops.put("sasl.client.callback.handler.class","software.amazon.msk.auth.iam.IAMClientCallbackHandler");
        return new KafkaAdmin(configprops);
    }

    @Bean
    public NewTopic requestTopic() {
        return new NewTopic("Requests", 100, (short) 2);
    }
    @Bean
    public NewTopic replyTopic() {
        return new NewTopic("Replies", 100, (short) 2);
    }
    @Bean
    public NewTopic testTopic() {
        return new NewTopic("perf-test", 100, (short) 2);
    }
}
