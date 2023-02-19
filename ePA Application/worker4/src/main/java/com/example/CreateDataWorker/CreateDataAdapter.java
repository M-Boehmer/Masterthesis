package com.example.CreateDataWorker;

import com.example.core.models.Patient;
import com.example.core.models.Reply;
import com.example.core.models.Request;
import com.example.core.models.Vaccination;
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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.example.core.services.ZeebeService.zeebeThrowError;


@Component
public class CreateDataAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(CreateDataAdapter.class);


    @Autowired
    private PatientService patientService;

    @Autowired
    private VaccinationService vaccinationService;

    @Autowired
    private KafkaService kafkaService;

    @Qualifier("zeebeClientLifecycle")
    @Autowired
    private ZeebeClient client;

    @JobWorker(type="Create_data",autoComplete = false)
    public void createData(final ActivatedJob job) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode parent = objectMapper.readTree(job.getVariables());
        Request request = objectMapper.readValue(parent.get("Request").toString(), Request.class);
        String request_table = request.getRequestTable();

        switch (request_table) {
            case "Patient":
                if (request.getAction().equals("Create")){
                    if (patientService.getPatient(request.getPatientID()).orElse(null) != null){
                        zeebeThrowError(client,job.getElementInstanceKey(),"A patient already exists with the given ID.",LOG);
                    }
                    List<Patient> patientList = patientService.getPatientByNameAndSurname(request.getPatient().getName(),request.getPatient().getSurname());
                    for (Patient patient : patientList) {
                        if (Objects.equals(patient.getBirthday(), request.getPatient().getBirthday()) && Objects.equals(patient.getStreet(), request.getPatient().getStreet())){
                            zeebeThrowError(client,job.getElementInstanceKey(),"A patient with the same name,surname,birthday and address already exists with the patientID: " +patient.getPatientID(),LOG);
                        }
                    }
                    Patient patientToAdd = request.getPatient();
                    patientToAdd.setPatientID(request.getPatientID());
                    patientService.addPatient(patientToAdd);
                } else {
                    zeebeThrowError(client,job.getElementInstanceKey(),"Requested database command not valid. Try again with a valid input. Possible commands include: Create",LOG);
                }
                break;
            case "Vaccination":

                if (request.getAction().equals("Create")){
                    List<Vaccination> vaccinationList = vaccinationService.getAllVaccinationByPatientID(request.getPatientID());
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    List<Integer> idList = new ArrayList<>();
                    LocalDate currentDate = LocalDate.now();
                    LocalDate currentDateMinus3Months = currentDate.minusMonths(3);
                    //currentDateMinus3Months = LocalDate.parse(currentDateMinus3Months.toString(),formatter);
                    for (Vaccination vaccination : vaccinationList) {
                        idList.add(Integer.valueOf(vaccination.getVaccinationID()));
                        try {
                            currentDate = LocalDate.parse(vaccination.getDate(),formatter);
                        } catch (DateTimeParseException e){
                            currentDate = LocalDate.now();
                        }
                        if (currentDate.isAfter(currentDateMinus3Months) && Objects.equals(vaccination.getVaccinationAgainst(), request.getVaccination().getVaccinationAgainst())){
                            zeebeThrowError(client,job.getElementInstanceKey(),"The requested vaccination already exists and was registered less than 3 months ago.",LOG);
                        }
                    }
                    Vaccination vaccination = request.getVaccination();
                    vaccination.setPractitionerID(request.getPractitionerID());
                    if (idList.isEmpty()){
                        vaccination.setVaccinationID("1");
                    } else {
                        vaccination.setVaccinationID(String.valueOf(Collections.max(idList) +1));
                    }
                    vaccination.setPatientID(request.getPatientID());
                    vaccinationService.addVaccination(vaccination);
                } else {
                    zeebeThrowError(client,job.getElementInstanceKey(),"Requested database command not valid. Try again with a valid input. Possible commands include: Create",LOG);
                }
                break;
            default:
                zeebeThrowError(client,job.getElementInstanceKey(),"Requested database table not valid. Try again with a valid input. Possible commands include: Patient,Practitioner,Vaccination ",LOG);
        }
        Reply reply = new Reply(request.getId(),"Successfully added data to table: "+ request.getRequestTable());
        kafkaService.sendMessage(reply,"Replies");
        ZeebeService.zeebeCompleteWithoutVariables(client,job,LOG);
    }
}
