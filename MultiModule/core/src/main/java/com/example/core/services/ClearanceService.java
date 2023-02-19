package com.example.core.services;

import com.example.core.models.Clearance;
import com.example.core.repository.ClearanceRepository;
import javax.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class ClearanceService {

    @Autowired
    ClearanceRepository ClearanceRepository;

    final Clearance.CompositeKey compositeKey = new Clearance.CompositeKey();

    public Optional<Clearance> getClearanceByCompositeKey(String patientID, String practitionerID) {
        compositeKey.setPatientID(patientID);
        compositeKey.setPractitionerID(practitionerID);
        return ClearanceRepository.findById(compositeKey);
    }

    public List<Clearance> getAllClearanceByPatientID(String patientID){
        return ClearanceRepository.findAllByPatientID(patientID);
    }

    public Clearance updateClearance(Clearance clearance, String patientID,String practitionerID) {
        compositeKey.setPatientID(patientID);
        compositeKey.setPractitionerID(practitionerID);
        boolean exists = ClearanceRepository.existsById(compositeKey);
        if(!exists) {
            throw new EntityNotFoundException("Patient record with Practitioner ID :"+practitionerID+" and patientID: "+patientID+" not found");
        } else {
            clearance.setPatientID(patientID);
            clearance.setPractitionerID(practitionerID);
        }
        return ClearanceRepository.save(clearance);
    }

    public void deleteClearance(String patientID,String practitionerID) {
        compositeKey.setPatientID(patientID);
        compositeKey.setPractitionerID(practitionerID);
        boolean exists = ClearanceRepository.existsById(compositeKey);
        if(!exists) {
            throw new EntityNotFoundException("Clearance with Practitioner ID :"+practitionerID+" and patientID: "+patientID+" not found");
        }
        else
            ClearanceRepository.deleteById(compositeKey);
    }

    public Clearance addClearance(Clearance clearance) {
        return ClearanceRepository.save(clearance);
    }

    public static boolean checkClearance(String patientID, String practitionerID, Clearance clearance, String requested_clearance, String type, Logger LOG) {
        try {
            if (clearance == null) {
                LOG.info("The Practitioner with ID: " + practitionerID + " does not have any clearances for the patient with ID: " + patientID);
                return false;
            } else if (!clearance.getClearanceMap().containsKey(requested_clearance)) {
                LOG.info("The Practitioner with ID: " + practitionerID + " does not have the specified clearance for the patient with ID: " + patientID);
                return false;
            } else if (type.equals("Check")) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
                LocalDate now = LocalDate.now();
                LocalDate until = LocalDate.parse(clearance.getClearanceMap().get(requested_clearance), formatter);
                if (now.isBefore(until)) {// i hate date objects
                    LOG.info("The Clearance for the Practitioner with ID: " + practitionerID + " has expired on the : " + clearance.getClearanceMap().get(requested_clearance));
                    return true;
                }
            }
            return true;
        }catch (Exception e){
            return false;
        }
    }
}
