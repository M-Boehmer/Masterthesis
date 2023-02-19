package com.example.CheckRequestValidityWorker;

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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.example.core.services.ClearanceService.checkClearance;
import static com.example.core.services.ZeebeService.zeebeThrowError;

@Component
public class CheckRequestValidityAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(CheckRequestValidityAdapter.class);
    @Autowired
    private PractitionerService practitionerService;
    @Autowired
    private PatientRecordService patientRecordService;
    @Autowired
    private PatientService patientService;
    @Autowired
    private ClearanceService clearanceService;

    @Qualifier("zeebeClientLifecycle")
    @Autowired
    private ZeebeClient client;

    @JobWorker(type = "Check_request_validity", autoComplete = false)
    public void checkAccessValidity(final ActivatedJob job) throws JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode parent = objectMapper.readTree(job.getVariables());
        Request request = objectMapper.readValue(parent.get("Request").toString(), Request.class);

        String practitionerID = request.getPractitionerID();
        String patientID = request.getPatientID();
        String requesterID = request.getRequesterID(); // Used for differentiating practitioners and patients as they are only able to edit their own database entries.
        String requester = request.getRequester();
        String requestType = request.getRequestType();
        boolean validPractitioner = Objects.equals(requester, "Practitioner") && Objects.equals(requesterID, practitionerID);
        boolean validPatient = Objects.equals(requester, "Patient") && Objects.equals(requesterID, patientID);
        Practitioner practitioner = new Practitioner();
        final Map<String, Object> attributes = new HashMap<>();

        if (practitionerID != null) {
            practitioner = practitionerService.getPractitioner(practitionerID).orElse(null);
            if (practitioner == null) {
                zeebeThrowError(client, job.getElementInstanceKey(), "No practitioner found with the given id: " + practitionerID, LOG);
            }
        }
        if (patientID != null) {
            Patient patient = patientService.getPatient(patientID).orElse(null);
            if (patient == null && !(job.getBpmnProcessId().equals("Create_data")) && !(request.getRequestTable().equals("Practitioner"))) { // Only do this check when Create Data isn't called, as it is possible that we want to create a patient
                zeebeThrowError(client, job.getElementInstanceKey(), "No patient found with the given id: " + patientID, LOG);
            }
        }
        if (patientID != null && practitionerID != null) {
            PatientRecord patientRecord = patientRecordService.getPatientRecord(practitionerID, patientID).orElse(null);
            if (patientRecord == null && job.getBpmnProcessId().contains("access") || job.getBpmnProcessId().contains("Create_data")) {
                LOG.info("No patient record found for PractitionerID: " + practitionerID + " in correlation to PatientID: " + patientID + ". Adding new Patient record");
                Date date = new Date();
                SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
                PatientRecord pr = new PatientRecord(new PatientRecord.CompositeKey(practitionerID, patientID), formatter.format(date));
                patientRecordService.addPatientRecord(pr);
            } else if (patientRecord == null && !(job.getBpmnProcessId().equals("Create_data")) && !(request.getRequestTable().equals("Practitioner"))) {
                zeebeThrowError(client, job.getElementInstanceKey(), "No patient record found for PractitionerID: " + practitionerID + " in correlation to PatientID: " + patientID + ". Grant Access to the Practitioner first to create a new PatientRecord", LOG);
            }
        }

        String request_table;
        switch (job.getBpmnProcessId()) {
            case "Grant_access":
                if (validPatient) {
                    switch (requestType) {
                        //For now only the Vaccination Clearance request is implemented because of the scope of the Thesis.
                        //Other Functions can be added by adding additional cases.
                        case "Vaccination_passport":
                            if (practitioner.getMedicalSpeciality().equals("Family doctor") || practitioner.getMedicalSpeciality().equals("Emergency doctor") || practitioner.getMedicalSpeciality().equals("Hospital doctor")) {
                                LOG.info("Valid request with PractitionerID: " + practitionerID + " for " + requestType);
                            } else {
                                zeebeThrowError(client, job.getElementInstanceKey(), "Access grant requests for vaccination passport are only valid from medical professionals with specialities as Family,Emergency or Hospital Doctor\n The practitioner with PractitionerID: " + practitionerID + " does not fulfill this criteria. ", LOG);
                            }
                            break;
                        default:
                            zeebeThrowError(client, job.getElementInstanceKey(), "For now only the Vaccination Clearance request is implemented. Every other request will be denied.", LOG);
                    }
                } else {
                    zeebeThrowError(client, job.getElementInstanceKey(), "Only valid patients have create and update access for their own clearances.", LOG);
                }
                ZeebeService.zeebeCompleteWithoutVariables(client, job, LOG);
                break;

            case "Remove_access":
                if (validPatient) {
                    Clearance clearance = clearanceService.getClearanceByCompositeKey(patientID, practitionerID).orElse(null);
                    if (!checkClearance(patientID, practitionerID, clearance, requestType,"Remove", LOG)) {
                        zeebeThrowError(client, job.getElementInstanceKey(), "The selected practitioner with ID: " + practitionerID + " either doesnt have any clearances for the selected patient with ID: " + patientID + " or is missing the requested clearance.", LOG);
                    }
                } else {
                    zeebeThrowError(client, job.getElementInstanceKey(), "Only valid patients have remove access for their own clearances.", LOG);
                }
                //Maybe check for Doctor medical speciality? For now, it is assumed that only patients remove the access

                ZeebeService.zeebeCompleteWithoutVariables(client, job, LOG);
                break;

            case "Read_data":
                request_table = request.getRequestTable();

                if (validPatient || validPractitioner) {
                    switch (request_table) {
                        case "Clearance":
                            if (Objects.equals(requester, "Patient")) {
                                LOG.info("Valid request, patient has CRUD access rights on Clearance table.");
                            } else if (Objects.equals(requester, "Practitioner")) {
                                if (Objects.equals(requesterID, practitionerID)) {
                                    LOG.info("Valid request, practitioner has R access rights on Clearance table.");
                                } else {
                                    zeebeThrowError(client, job.getElementInstanceKey(), "Invalid request, practitioner only has R access rights on his own table entries concerning his own ID.", LOG);
                                }
                            }
                            break;
                        case "Patient":
                            if (Objects.equals(requester, "Patient")) {
                                LOG.info("Valid request, patient has R access rights on Patient table.");
                            } else if (Objects.equals(requester, "Practitioner")) {
                                LOG.info("Valid request, practitioner has CRUD access rights on Patient table.");
                            }
                            break;
                        case "PatientRecord":
                            zeebeThrowError(client, job.getElementInstanceKey(), "Invalid request. Neither practitioner nor patient have any access rights to PatientRecord table.", LOG);

                        case "Practitioner":
                            if (Objects.equals(requester, "Patient")) {
                                zeebeThrowError(client, job.getElementInstanceKey(), "Invalid request, patient has no access rights on Practitioner table.", LOG);
                            } else if (Objects.equals(requester, "Practitioner")) {
                                if (!Objects.equals(practitionerID, requesterID)) {
                                    zeebeThrowError(client, job.getElementInstanceKey(), "Invalid request, practitioner only has RU access rights on his own table entry.", LOG);
                                }
                                LOG.info("Valid request, practitioner has RUD access rights on his own table entry.");
                            }
                            break;
                        case "Vaccination":
                            if (Objects.equals(requester, "Patient")) {
                                LOG.info("Valid request, patient has R access rights on Vaccination table.");
                            } else if (Objects.equals(requester, "Practitioner")) {
                                LOG.info("Valid request, practitioner has CRUD access rights on Vaccination table.");
                            }
                            break;
                        default:
                            zeebeThrowError(client, job.getElementInstanceKey(), "Invalid request, requested table does not exist.", LOG);
                    }
                } else {
                    zeebeThrowError(client, job.getElementInstanceKey(), "Invalid request, requester variable does not match practitioner or patient, or has an invalid ID. Retry with valid input", LOG);
                }

                ZeebeService.zeebeCompleteWithoutVariables(client, job, LOG);
                break;
            case "Create_data":
            case "Update_data":
                request_table = request.getRequestTable();

                if (validPatient || validPractitioner) {
                    switch (request_table) {
                        case "Clearance":
                            if (Objects.equals(requester, "Practitioner")) {
                                zeebeThrowError(client, job.getElementInstanceKey(), "Invalid request, practitioner has only R access rights on Clearances.", LOG);
                            }
                            zeebeThrowError(client, job.getElementInstanceKey(), "Invalid request, for CUD actions concerning clearances use the dedicated clearances actions.", LOG);
                        case "Patient":
                            if (Objects.equals(requester, "Patient")) {
                                zeebeThrowError(client, job.getElementInstanceKey(), "Invalid request, patient has only R access rights on Patient table.", LOG);
                            } else if (Objects.equals(requester, "Practitioner")) {
                                LOG.info("Valid request, practitioner has CRUD access rights on Patient table.");
                            }
                            break;
                        case "PatientRecord":
                            zeebeThrowError(client, job.getElementInstanceKey(), "Invalid request. Neither practitioner nor patient have any access rights to PatientRecord table.", LOG);

                        case "Practitioner":
                            if (Objects.equals(requester, "Patient")) {
                                zeebeThrowError(client, job.getElementInstanceKey(), "Invalid request, patient has no access rights on Practitioner table.", LOG);
                            } else if (Objects.equals(requester, "Practitioner")) {
                                if (!practitionerID.equals(requesterID)) {
                                    zeebeThrowError(client, job.getElementInstanceKey(), "Invalid request, practitioner only has RU access rights on his own table entry.", LOG);
                                }
                                LOG.info("Valid request, practitioner has UD access rights on his own table entry.");
                            }
                            break;

                        case "Vaccination":
                            if (Objects.equals(requester, "Patient")) {
                                zeebeThrowError(client, job.getElementInstanceKey(), "Invalid request, patient has only R access rights on Vaccination table.", LOG);
                            } else if (Objects.equals(requester, "Practitioner")) {
                                LOG.info("Valid request, practitioner has CRUD access rights on Vaccination table.");
                            }
                            break;
                        default:
                            zeebeThrowError(client,job.getElementInstanceKey(),"Invalid request, requested table does not exist.",LOG);
                    }
                } else {
                    zeebeThrowError(client, job.getElementInstanceKey(), "Invalid request, requester variable does not match practitioner or patient, or has an invalid ID. Retry with valid input", LOG);
                }
                ZeebeService.zeebeCompleteWithoutVariables(client, job, LOG);
                break;
            case "Delete_data":
                request_table = request.getRequestTable();

                if (validPatient || validPractitioner) {
                    switch (request_table) {
                        case "Clearance":
                            if (Objects.equals(requester, "Practitioner")) {
                                zeebeThrowError(client, job.getElementInstanceKey(), "Invalid request, practitioner has only R access rights on Clearances.", LOG);
                            }
                            zeebeThrowError(client, job.getElementInstanceKey(), "Invalid request, for CUD actions concerning clearances use the dedicated clearances actions.", LOG);
                        case "Patient":
                            if (Objects.equals(requester, "Patient")) {
                                zeebeThrowError(client, job.getElementInstanceKey(), "Invalid request, patient has only R access rights on Patient table.", LOG);
                            } else if (Objects.equals(requester, "Practitioner")) {
                                LOG.info("Valid request, practitioner has CRUD access rights on Patient table.");
                            }
                            break;
                        case "PatientRecord":
                            zeebeThrowError(client, job.getElementInstanceKey(), "Invalid request. Neither practitioner nor patient have any access rights to PatientRecord table.", LOG);

                        case "Practitioner":
                            if (Objects.equals(requester, "Patient")) {
                                zeebeThrowError(client, job.getElementInstanceKey(), "Invalid request, patient has no access rights on Practitioner table.", LOG);
                            } else if (Objects.equals(requester, "Practitioner")) {
                                zeebeThrowError(client, job.getElementInstanceKey(), "Invalid request, practitioner only has RU access rights on his own table entry.", LOG);
                            }
                            break;

                        case "Vaccination":
                            if (Objects.equals(requester, "Patient")) {
                                zeebeThrowError(client, job.getElementInstanceKey(), "Invalid request, patient has only R access rights on Vaccination table.", LOG);
                            } else if (Objects.equals(requester, "Practitioner")) {
                                LOG.info("Valid request, practitioner has CRUD access rights on Vaccination table.");
                            }
                            break;
                        default:
                            zeebeThrowError(client,job.getElementInstanceKey(),"Invalid request, requested table does not exist.",LOG);
                    }
                } else {
                    zeebeThrowError(client, job.getElementInstanceKey(), "Invalid request, requester variable does not match practitioner or patient, or has an invalid ID. Retry with valid input", LOG);
                }
                ZeebeService.zeebeCompleteWithoutVariables(client, job, LOG);
                break;
            default:
                zeebeThrowError(client, job.getElementInstanceKey(), "Invalid request, requested action does not exist. Retry with valid input", LOG);
        }

    }
}

