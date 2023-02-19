package com.example.UpdateDataWorker;

import com.example.core.models.*;
import com.example.core.services.*;
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

import java.util.HashMap;
import java.util.Map;

import static com.example.core.services.ZeebeService.zeebeThrowError;

@Component
public class UpdateDataAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(UpdateDataAdapter.class);


    @Autowired
    private PatientService patientService;

    @Autowired
    private KafkaService kafkaService;

    @Autowired
    private PractitionerService practitionerService;

    @Autowired
    private VaccinationService vaccinationService;

    @Qualifier("zeebeClientLifecycle")
    @Autowired
    private ZeebeClient client;

    @JobWorker(type = "Update_data", autoComplete = false)
    public void updateData(final ActivatedJob job) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode parent = objectMapper.readTree(job.getVariables());
        Request request = objectMapper.readValue(parent.get("Request").toString(), Request.class);
        String request_table = request.getRequestTable();
        Map<String,Object> attributes = new HashMap<>();

        switch (request_table) {
            case "Practitioner":
                if (request.getAction().equals("Update")) {
                    Practitioner practitioner = practitionerService.getPractitioner(request.getPractitionerID()).orElse(null);
                    request.getPractitioner().setPractitionerID(request.getPractitionerID());
                    if (request.getPractitioner().equals(practitioner)){
                        zeebeThrowError(client,job.getElementInstanceKey(),"No values were changed. Change any values and try again",LOG);
                    }
                    if (practitioner != null) {
                        practitioner = request.getPractitioner();
                        practitionerService.updatePractitioner(practitioner, practitioner.getPractitionerID());
                    } else {
                        zeebeThrowError(client,job.getElementInstanceKey(),"The requested Practitioner with the ID: " + request.getRequesterID() + " does not exist. Try again with a valid ID",LOG);
                    }
                } else {
                    zeebeThrowError(client,job.getElementInstanceKey(),"Requested database command not valid. Try again with a valid input. Possible commands include: Update",LOG);
                }
                break;
            case "Patient":
                if (request.getAction().equals("Update")) {
                    Patient patient = patientService.getPatient(request.getPatientID()).orElse(null);
                    request.getPatient().setPatientID(request.getPatientID());
                    if (request.getPatient().equals(patient)){
                        zeebeThrowError(client,job.getElementInstanceKey(),"No values were changed. Change any values and try again",LOG);
                    }
                    if (patient != null) {
                        patientService.updatePatient(request.getPatient(),request.getPatient().getPatientID());
                    } else {
                        zeebeThrowError(client,job.getElementInstanceKey(),"The requested Patient with the ID: " + request.getPatientID() + " does not exist. Try again with a valid ID",LOG);
                    }

                } else {
                    zeebeThrowError(client,job.getElementInstanceKey(),"Requested database command not valid. Try again with a valid input. Possible commands include: Update",LOG);
                }
                break;
            case "Vaccination":
                if (request.getAction().equals("Update")) {
                    Vaccination vaccination = vaccinationService.getVaccinationByCompositeKey(request.getPatientID(),request.getVaccination().getVaccinationID()).orElse(null);
                    request.getVaccination().setPatientID(request.getPatientID());
                    request.getVaccination().setPractitionerID(request.getPractitionerID());
                    if (request.getVaccination().equals(vaccination)){
                        zeebeThrowError(client,job.getElementInstanceKey(),"No values were changed. Change any values and try again",LOG);
                    }
                    if (vaccination != null) {
                        vaccinationService.updateVaccination(request.getVaccination(),request.getPatientID(),request.getVaccination().getVaccinationID());
                    } else {
                        zeebeThrowError(client,job.getElementInstanceKey(),"The requested Vaccination with the patientID: " + request.getPatientID() + " and vaccinationID: "+ request.getVaccination().getVaccinationID()+ " does not exist. Try again with a valid ID",LOG);
                    }
                } else {
                    zeebeThrowError(client,job.getElementInstanceKey(),"Requested database command not valid. Try again with a valid input. Possible commands include: Update",LOG);
                }
                break;
            default:
                zeebeThrowError(client,job.getElementInstanceKey(),"Requested database table not valid. Try again with a valid input. Possible commands include: Patient,Practitioner,Vaccination",LOG);
        }
        Reply reply = new Reply(request.getId(),"Successfully updated data in table: "+ request.getRequestTable());
        kafkaService.sendMessage(reply,"Replies");
        ZeebeService.zeebeCompleteWithoutVariables(client, job, LOG);
    }
}
