package com.example.core.services;

import com.example.core.models.Patient;
import com.example.core.repository.PatientRepository;

import javax.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Optional;

@Service
public class PatientService {

    @Autowired
    PatientRepository PatientRepository;

    public Optional<Patient> getPatient(String id) {
        return PatientRepository.findById(id);
    }
    public List<Patient> getPatientByNameAndSurname(String Name, String Surname){
        return PatientRepository.getPatientsByNameAndSurname(Name,Surname);
    }
    public List<Patient> getAllPatients(){
        return (List<Patient>) PatientRepository.findAll();
    }

    public Patient updatePatient(Patient Patient, String id) {
        boolean exists = PatientRepository.existsById(id);
        if(!exists) {
            throw new EntityNotFoundException("Patient(id- " + id + ") Not Found !!");
        }
        else
            Patient.setPatientID(id);
        return PatientRepository.save(Patient);
    }

    public void deletePatient(String id) {
        boolean exists = PatientRepository.existsById(id);
        if(!exists) {
            throw new EntityNotFoundException("Patient(id- " + id + ") Not Found !!");
        }
        else
            PatientRepository.deleteById(id);
    }

    public Patient addPatient(Patient patient) {
        return PatientRepository.save(patient);
    }
}
