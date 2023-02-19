package com.example.ReadDataWorker;

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

import java.util.List;
import java.util.Map;

import static com.example.core.services.ZeebeService.zeebeThrowError;


@Component
public class ReadDataAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(ReadDataAdapter.class);

    @Autowired
    private KafkaService kafkaService;

    @Autowired
    private ClearanceService clearanceService;

    @Autowired
    private PatientService patientService;

    @Autowired
    private PractitionerService practitionerService;

    @Autowired
    private VaccinationService vaccinationService;

    @Qualifier("zeebeClientLifecycle")
    @Autowired
    private ZeebeClient client;

    @JobWorker(type="Return_read_data",autoComplete = false)
    public void returnReadData(final ActivatedJob job) throws JsonProcessingException {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode parent = objectMapper.readTree(job.getVariables());
            Request request = objectMapper.readValue(parent.get("Request").toString(), Request.class);
            String request_table = request.getRequestTable();
            Reply reply = new Reply();
            reply.setId(request.getId());
            StringBuilder output = new StringBuilder();

            switch (request_table) {
                case "Clearance":
                    if (request.getAction().equals("FindByID") && request.getPractitionerID() != null) {
                        Clearance clearance;
                        clearance = clearanceService.getClearanceByCompositeKey(request.getPatientID(), request.getPractitionerID()).orElse(null);
                        output.append("Clearances for patient with id: ").append(clearance.getPatientID()).append("and practitioner with id: ").append(clearance.getPractitionerID());
                        for (Map.Entry<String, String> pair : clearance.getClearanceMap().entrySet()) {
                            output.append("\n Clearance: ").append(pair.getKey()).append("  Duration: ").append(pair.getValue());
                        }
                    } else if (request.getAction().equals("FindAllByID") || request.getPractitionerID() == null) {
                        List<Clearance> result;
                        result = clearanceService.getAllClearanceByPatientID(request.getPatientID());
                        output.append("All clearances for patient with id: ").append(request.getPatientID());
                        for (Clearance clearance : result) {
                            output.append("\n practitioner with ID: ").append(clearance.getPractitionerID());
                            for (Map.Entry<String, String> pair : clearance.getClearanceMap().entrySet()) {
                                output.append("\n\t Clearance: ").append(pair.getKey()).append("  Duration: ").append(pair.getValue());
                            }
                        }
                    } else {
                        zeebeThrowError(client, job.getElementInstanceKey(), " Requested database command not valid. Try again with a valid input. Possible Commands include: FindByID, FindAllByID", LOG);
                    }
                    break;
                case "Patient":
                    if (request.getAction().equals("FindByID")) {
                        Patient patient = patientService.getPatient(request.getPatientID()).orElse(null);
                        output = new StringBuilder("Data for patient with ID: " + request.getPatientID() +
                                "\n\tName: " + patient.getName() +
                                "\n\tSurname: " + patient.getSurname() +
                                "\n\tBirthday: " + patient.getBirthday() +
                                "\n\tStreet: " + patient.getStreet() +
                                "\n\tCity: " + patient.getCity() +
                                "\n\tPostal code: " + patient.getPostalCode() +
                                "\n\tPhone number: " + patient.getPhoneNumber() +
                                "\n\tEmail: " + patient.getEmail());
                    } else {
                        zeebeThrowError(client, job.getElementInstanceKey(), "Requested database command not valid. Try again with a valid input. Possible Commands include: FindByID", LOG);
                    }
                    break;
                case "Practitioner":
                    if (request.getAction().equals("FindByID")) {
                        Practitioner practitioner = practitionerService.getPractitioner(request.getPractitionerID()).orElse(null);

                        output = new StringBuilder("Data for Practitioner with ID: " + request.getPractitionerID() +
                                "\n\tName: " + practitioner.getName() +
                                "\n\tSurname: " + practitioner.getSurname() +
                                "\n\tMedical speciality: " + practitioner.getMedicalSpeciality() +
                                "\n\tStreet: " + practitioner.getStreet() +
                                "\n\tCity: " + practitioner.getCity() +
                                "\n\tPostal code: " + practitioner.getPostalCode() +
                                "\n\tPhone number: " + practitioner.getPhoneNumber() +
                                "\n\tEmail: " + practitioner.getEmail());
                    } else {
                        zeebeThrowError(client, job.getElementInstanceKey(), " Requested database command not valid. Try again with a valid input. Possible Commands include: FindByID", LOG);
                    }
                    break;
                case "Vaccination":
                    if (request.getAction().equals("FindByID")) {
                        Vaccination vaccination = vaccinationService.getVaccinationByCompositeKey(request.getPatientID(), request.getVaccination().getVaccinationID()).orElse(null);
                        output = new StringBuilder("Data for Vaccination with patient ID: " + request.getPatientID() + " and vaccination ID: " + vaccination.getVaccinationID() +
                                "\n\tDate: " + vaccination.getDate() +
                                "\n\tManufacturer: " + vaccination.getManufacturer() +
                                "\n\tBatch Nr: " + vaccination.getBatchNr() +
                                "\n\tAdministering practitioner: " + vaccination.getPractitionerID() +
                                "\n\tVaccination against disease : " + vaccination.getVaccinationAgainst());

                        LOG.info(vaccination.getVaccinationAgainst() + " " + vaccination.getBatchNr());
                    } else if (request.getAction().equals("FindAllByID")) {
                        List<Vaccination> result;
                        result = vaccinationService.getAllVaccinationByPatientID(request.getPatientID());
                        output.append("All vaccinations for patient with id: ").append(request.getPatientID());
                        for (Vaccination vaccination : result) {
                            output.append("\n Vaccination with ID: ").append(vaccination.getVaccinationID()).append("\n\tDate: ").append(vaccination.getDate()).append("\n\tManufacturer: ").append(vaccination.getManufacturer()).append("\n\tBatch Nr: ").append(vaccination.getBatchNr()).append("\n\tAdministering practitioner: ").append(vaccination.getPractitionerID()).append("\n\tVaccination against disease : ").append(vaccination.getVaccinationAgainst());
                        }
                    } else {
                        zeebeThrowError(client, job.getElementInstanceKey(), " Requested database command not valid. Try again with a valid input. Possible Commands include: FindByID, FindAllByID", LOG);
                    }
                    break;
                default:
                    zeebeThrowError(client, job.getElementInstanceKey(), " Requested database table command not valid. Try again with a valid input. Possible Commands include: Clearance,Patient,Practitioner,Vaccination ", LOG);
            }
            reply.setReply(output.toString());
            kafkaService.sendMessage(reply, "Replies");
            ZeebeService.zeebeCompleteWithoutVariables(client, job, LOG);
        }catch (Exception e){
            ZeebeService.zeebeThrowError(client,job.getElementInstanceKey(),e.toString(),LOG);
        }
    }
}
