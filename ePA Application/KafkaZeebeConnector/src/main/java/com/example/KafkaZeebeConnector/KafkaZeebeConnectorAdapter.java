package com.example.KafkaZeebeConnector;


import com.example.core.models.Request;
import com.example.core.services.ZeebeService;
import io.camunda.zeebe.client.ZeebeClient;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class KafkaZeebeConnectorAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(KafkaZeebeConnectorAdapter.class);

    @Qualifier("zeebeClientLifecycle")
    @Autowired
    private ZeebeClient client;


    @KafkaListener(topics = "Requests", groupId = "requester-group")
    public void sendKafkaToZeebe(Request request){
        Map<String,Object> attributes = Map.of("Request",request);
        try {
            switch (request.getAction()) {

                case "Create" -> ZeebeService.zeebeActivateJob(client, "Create_data", attributes, LOG);
                case "Delete" -> ZeebeService.zeebeActivateJob(client, "Delete_data", attributes, LOG);
                case "Update" -> ZeebeService.zeebeActivateJob(client, "Update_data", attributes, LOG);
                case "FindByID", "FindAllByID" -> ZeebeService.zeebeActivateJob(client, "Read_data", attributes, LOG);
                case "RemoveAccess" -> ZeebeService.zeebeActivateJob(client, "Remove_access", attributes, LOG);
                case "GrantAccess" -> ZeebeService.zeebeActivateJob(client, "Grant_access", attributes, LOG);
                default -> LOG.error(request.getAction() + " is not a valid Action to take.");
            }
        } catch (StatusRuntimeException e){

        }
    }
}
