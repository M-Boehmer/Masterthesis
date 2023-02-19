package com.example.GrantAccessRightsWorker;

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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Component
public class GrantAccessRightsAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(GrantAccessRightsAdapter.class);

    @Autowired
    private ClearanceService clearanceService;

    @Autowired
    private KafkaService kafkaService;

    @Qualifier("zeebeClientLifecycle")
    @Autowired
    private ZeebeClient client;

    @JobWorker(type="Grant_access_rights",autoComplete = false)
    public void grantAccessRights(final ActivatedJob job) throws JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode parent = objectMapper.readTree(job.getVariables());
        Request request = objectMapper.readValue(parent.get("Request").toString(), Request.class);


        String practitionerID = request.getPractitionerID();
        String patientID = request.getPatientID();
        String requested_access = request.getRequestType();
        String requested_access_period = request.getRequestedAccessPeriod();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        Clearance.CompositeKey compositeKey = new Clearance.CompositeKey(patientID,practitionerID);
        Map<String,String> clearanceMap = new HashMap<>();
        Clearance c = clearanceService.getClearanceByCompositeKey(patientID,practitionerID).orElse(null);
        String period = "";
        Reply reply = new Reply();
        reply.setId(request.getId());


        if (c == null || c.getClearanceMap().isEmpty()){
            LOG.info("The Practitioner with ID: "+ practitionerID+ " does not have any clearances for the patient with ID: "+patientID);
            LocalDate until = LocalDate.now();
            until = until.plusMonths(Integer.parseInt(requested_access_period));
            period = until.format(formatter);
            clearanceMap.put(requested_access,period);
            c = new Clearance(compositeKey,clearanceMap);
            clearanceService.addClearance(c);

        } else if (c.getClearanceMap().getOrDefault(requested_access,"No access").equals("indefinite")){
            ZeebeService.zeebeThrowError(client,job.getProcessInstanceKey(),"The practitioner with ID: "+practitionerID+" already has indefinite "+requested_access+" Access for the patient with ID: "+patientID,LOG);
        } else {
            if (requested_access.equals("indefinite")){
                period = requested_access_period;
                c.getClearanceMap().put(requested_access,requested_access_period);
            } else {
                LocalDate until = LocalDate.parse(c.getClearanceMap().get(requested_access),formatter);
                until = until.plusMonths(Integer.parseInt(requested_access_period));
                period = until.format(formatter);
                c.getClearanceMap().put(requested_access,period);

            }
            clearanceService.addClearance(c);
        }
        reply.setReply("The practitioner with ID: "+practitionerID+" was to added to the Clearances for the patient with ID: "+patientID+ " until: "+ period);
        kafkaService.sendMessage(reply,"Replies");
        ZeebeService.zeebeCompleteWithoutVariables(client,job,LOG);
    }
}
