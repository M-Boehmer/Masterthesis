package com.example.CheckAccessRightsWorker;

import com.example.core.models.Clearance;
import com.example.core.models.Request;
import com.example.core.services.ClearanceService;
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

import static com.example.core.services.ClearanceService.checkClearance;
import static com.example.core.services.ZeebeService.zeebeThrowError;

@Component
public class CheckAccessRightsAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(CheckAccessRightsAdapter.class);

    @Autowired
    private ClearanceService clearanceService;

    @Qualifier("zeebeClientLifecycle")
    @Autowired
    private ZeebeClient client;

    @JobWorker(type="Check_access_rights",autoComplete = false)
    public void checkAccessRights(final ActivatedJob job) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode parent = objectMapper.readTree(job.getVariables());
        Request request = objectMapper.readValue(parent.get("Request").toString(), Request.class);

        String practitionerID = request.getPractitionerID();
        String patientID = request.getPatientID();
        String request_type = request.getRequestType(); // e.g. Vaccination passport

        Clearance clearance = clearanceService.getClearanceByCompositeKey(patientID,practitionerID).orElse(null);

        //überprüfung bzgl zugriffen auf andere Practitioner IDS hierhin verlagern, da dass eher Access bezogen ist

        if (checkClearance(practitionerID,patientID,clearance,request_type,"Check",LOG)){
            LOG.info("Access rights are granted, continuing.");
        } else {
            zeebeThrowError(client,job.getElementInstanceKey(),"The selected practitioner with ID: "+practitionerID+" either doesnt have any clearances for the selected patient with ID: "+patientID+" or is missing the required clearance. Please request clearance and try again.",LOG);
        }
        ZeebeService.zeebeCompleteWithoutVariables(client,job,LOG);
    }
}
