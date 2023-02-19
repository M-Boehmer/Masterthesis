package com.example.DeleteDataWorker;

import com.example.core.models.Reply;
import com.example.core.models.Request;
import com.example.core.services.KafkaService;
import com.example.core.services.PatientService;
import com.example.core.services.VaccinationService;
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

import static com.example.core.services.ZeebeService.zeebeThrowError;


@Component
public class DeleteDataAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(DeleteDataAdapter.class);

    @Autowired
    private KafkaService kafkaService;

    @Autowired
    private PatientService patientService;

    @Autowired
    private VaccinationService vaccinationService;

    @Qualifier("zeebeClientLifecycle")
    @Autowired
    private ZeebeClient client;


    @JobWorker(type="Delete_data",autoComplete = false)
    public void deleteData(final ActivatedJob job) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode parent = objectMapper.readTree(job.getVariables());
        Request request = objectMapper.readValue(parent.get("Request").toString(), Request.class);
        String request_table = request.getRequestTable();
        switch (request_table) {
            case "Patient":
                if (request.getAction().equals("Delete")){
                    if (patientService.getPatient(request.getPatientID()).orElse(null) == null){
                        zeebeThrowError(client,job.getElementInstanceKey(),"No patient exists with the given ID.",LOG);
                    }
                    patientService.deletePatient(request.getPatientID());
                } else {
                    zeebeThrowError(client,job.getElementInstanceKey(),"Requested database command not valid. Try again with a valid input. Possible commands include: Delete",LOG);
                }
                break;
            case "Vaccination":
                if (request.getAction().equals("Delete")){
                    vaccinationService.deleteVaccination(request.getPatientID(),request.getVaccination().getVaccinationID());
                } else {
                    zeebeThrowError(client,job.getElementInstanceKey(),"Requested database command not valid. Try again with a valid input. Possible commands include: Delete",LOG);
                }
                break;
            default:
                zeebeThrowError(client,job.getElementInstanceKey(),"Requested database table not valid. Try again with a valid input. Possible commands include: Patient,Vaccination ",LOG);
        }
        Reply reply = new Reply(request.getId(),"Successfully deleted data from table: "+ request.getRequestTable());
        kafkaService.sendMessage(reply,"Replies");
        ZeebeService.zeebeCompleteWithoutVariables(client,job,LOG);
    }
}
