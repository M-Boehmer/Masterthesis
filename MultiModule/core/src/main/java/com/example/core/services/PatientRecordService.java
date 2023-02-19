package com.example.core.services;

import com.example.core.models.PatientRecord;
import com.example.core.models.Vaccination;
import com.example.core.repository.PatientRecordRepository;

import javax.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Optional;

@Service
public class PatientRecordService {

    @Autowired
    PatientRecordRepository patientRecordRepository;
    final PatientRecord.CompositeKey compositeKey = new PatientRecord.CompositeKey();

    public Optional<PatientRecord> getPatientRecord( String practitionerID,String patientID) {
        compositeKey.setPatientID(patientID);
        compositeKey.setPractitionerID(practitionerID);
        return patientRecordRepository.findById(compositeKey);
    }
    public List<PatientRecord> getAllPatientRecordByPractitionerID(String practitionerID){
        return patientRecordRepository.queryAllByPractitionerID(practitionerID);
    }
    public PatientRecord updatePatientRecord(PatientRecord patientRecord, String practitionerID,String patientID) {
        compositeKey.setPatientID(patientID);
        compositeKey.setPractitionerID(practitionerID);
        boolean exists = patientRecordRepository.existsById(compositeKey);
        if(!exists) {
            throw new EntityNotFoundException("Patient record with Practitioner ID :"+practitionerID+" and patientID: "+patientID+" not found");
        } else {
            patientRecord.setPatientID(patientID);
            patientRecord.setPractitionerID(practitionerID);
        }
        return patientRecordRepository.save(patientRecord);
    }

    public void deletePatientRecord(String practitionerID,String patientID) {
        compositeKey.setPatientID(patientID);
        compositeKey.setPractitionerID(practitionerID);
        boolean exists = patientRecordRepository.existsById(compositeKey);
        if(!exists) {
            throw new EntityNotFoundException("Patient record with Practitioner ID :"+practitionerID+" and patientID: "+patientID+" not found");
        }
        else
            patientRecordRepository.deleteById(compositeKey);
    }

    public PatientRecord addPatientRecord(PatientRecord patientRecord) {
        return patientRecordRepository.save(patientRecord);
    }
}
