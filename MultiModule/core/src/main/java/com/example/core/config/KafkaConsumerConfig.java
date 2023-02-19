package com.example.core.config;


import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Autowired
    private KafkaTemplate<String, Object> kafkatemplate;


    @Bean
    public ConsumerFactory<String, Object> jsonConsumerFactory() {
        Map<String, Object> configprops = new HashMap<>();
        configprops.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configprops.put(ConsumerConfig.GROUP_ID_CONFIG, "group-id");
        configprops.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configprops.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        configprops.put(JsonDeserializer.TRUSTED_PACKAGES,"com.example.core.models");
        //configprops.put("sasl.mechanism","SCRAM-SHA-512");
        //configprops.put("security.protocol", "SASL_SSL");
        //configprops.put("sasl.jaas.config","org.apache.kafka.common.security.scram.ScramLoginModule required username='kafka' password='kafka' awsDebugCreds=true;");
        //configprops.put("sasl.mechanism","AWS_MSK_IAM");
        //configprops.put("security.protocol", "SASL_SSL");
        //configprops.put("sasl.jaas.config","software.amazon.msk.auth.iam.IAMLoginModule required ;");
        //configprops.put("sasl.client.callback.handler.class","software.amazon.msk.auth.iam.IAMClientCallbackHandler");
        return new DefaultKafkaConsumerFactory<>(configprops);
    }
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object>
                factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(jsonConsumerFactory());
        return factory;
    }

}
