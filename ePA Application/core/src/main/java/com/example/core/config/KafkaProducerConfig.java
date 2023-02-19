package com.example.core.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaProducerConfig.class);

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;


    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> configprops = new HashMap<>();
        configprops.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,bootstrapServers);
        configprops.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configprops.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        //configprops.put("sasl.mechanism","SCRAM-SHA-512");
        //configprops.put("security.protocol", "SASL_SSL");
        //configprops.put("sasl.jaas.config","org.apache.kafka.common.security.scram.ScramLoginModule required username='kafka' password='kafka' awsDebugCreds=true;");

        //configprops.put("sasl.mechanism","AWS_MSK_IAM");
        //configprops.put("security.protocol", "SASL_SSL");
        //configprops.put("sasl.jaas.config","software.amazon.msk.auth.iam.IAMLoginModule required ;");
        //configprops.put("sasl.client.callback.handler.class","software.amazon.msk.auth.iam.IAMClientCallbackHandler");
        return new DefaultKafkaProducerFactory<>(configprops);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
