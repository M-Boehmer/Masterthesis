package com.example.ErrorHandlingWorker;

import com.example.core.models.Reply;
import com.example.core.models.Request;
import com.example.core.services.KafkaService;
import com.example.core.services.ZeebeService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class ErrorHandlingAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(ErrorHandlingAdapter.class);

    @Autowired
    KafkaService kafkaService = new KafkaService();

    @Qualifier("zeebeClientLifecycle")
    @Autowired
    private ZeebeClient client;

    @JobWorker(type="Handle_errors",autoComplete = false)
    public void handleErrors(final ActivatedJob job) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode parent = objectMapper.readTree(job.getVariables());
        Request request = objectMapper.readValue(parent.get("Request").toString(), Request.class);
        Reply reply = new Reply(request.getId(),(" Error: "+job.getVariablesAsMap().get("Error").toString()));

        LOG.info("sending Error to Kafka: " +reply.getReply());
        kafkaService.sendMessage(reply,"Replies");
        ZeebeService.zeebeCompleteWithoutVariables(client,job,LOG);
    }
}
