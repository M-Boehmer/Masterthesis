package com.example.RemoveAccessRightsWorker;

import com.example.core.models.Clearance;
import com.example.core.models.Reply;
import com.example.core.models.Request;
import com.example.core.services.ClearanceService;
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
public class RemoveAccessRightsAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(RemoveAccessRightsAdapter.class);

    @Autowired
    private KafkaService kafkaService;

    @Autowired
    private ClearanceService clearanceService;
    @Qualifier("zeebeClientLifecycle")
    @Autowired
    private ZeebeClient client;

    @JobWorker(type="Remove_access_rights",autoComplete = false)
    public void removeAccessRights(final ActivatedJob job) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode parent = objectMapper.readTree(job.getVariables());
        Request request = objectMapper.readValue(parent.get("Request").toString(), Request.class);

        String practitionerID = request.getPractitionerID();
        String patientID = request.getPatientID();
        String requestType = request.getRequestType();
        Reply reply = new Reply();
        reply.setId(request.getId());

        if ("RemoveAll".equals(requestType)) {
            clearanceService.deleteClearance(patientID, practitionerID);
        } else {
            Clearance clearance = clearanceService.getClearanceByCompositeKey(patientID, practitionerID).orElse(null);
            assert clearance != null;
            clearance.getClearanceMap().remove(requestType);
            clearanceService.updateClearance(clearance, patientID, practitionerID);
            reply.setReply("Requested clearance for practitioner with ID: "+ request.getPractitionerID() +" was successfully removed from patient with ID: "+ request.getPatientID());
        }

        kafkaService.sendMessage(reply,"Replies");
        ZeebeService.zeebeCompleteWithoutVariables(client,job,LOG);
    }
}
