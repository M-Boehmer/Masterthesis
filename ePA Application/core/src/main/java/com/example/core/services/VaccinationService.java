package com.example.core.services;

import com.example.core.models.Vaccination;
import com.example.core.repository.VaccinationRepository;
import javax.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class VaccinationService {

    @Autowired
    VaccinationRepository vaccinationRepository;
    final Vaccination.CompositeKey compositeKey = new Vaccination.CompositeKey();

    public Optional<Vaccination> getVaccinationByCompositeKey(String patientID, String vaccinationID) {
        compositeKey.setPatientID(patientID);
        compositeKey.setVaccinationID(vaccinationID);
        Optional<Vaccination> vaccination = vaccinationRepository.findById(compositeKey);
        if (vaccination.isPresent()) {
            return vaccination;
        }
        else
            throw new EntityNotFoundException("Vaccination with Patient ID :"+patientID+" and Vaccination ID: "+vaccinationID+" not found");
    }
    public List<Vaccination> getAllVaccinationByPatientID(String patientID){
        return vaccinationRepository.queryAllByPatientID(patientID);
    }
    public Vaccination updateVaccination(Vaccination vaccination, String patientID,String vaccinationID) {
        compositeKey.setPatientID(patientID);
        compositeKey.setVaccinationID(vaccinationID);
        boolean exists = vaccinationRepository.existsById(compositeKey);
        if(!exists) {
            throw new EntityNotFoundException("Vaccination with Patient ID :"+patientID+" and Vaccination ID: "+vaccinationID+" not found");
        } else {
            vaccination.setPatientID(patientID);
            vaccination.setVaccinationID(vaccinationID);
        }
        return vaccinationRepository.save(vaccination);
    }

    public void deleteVaccination(String patientID,String vaccinationID) {
        compositeKey.setPatientID(patientID);
        compositeKey.setVaccinationID(vaccinationID);
        boolean exists = vaccinationRepository.existsById(compositeKey);
        if(!exists) {
            throw new EntityNotFoundException("Vaccination with Patient ID :"+patientID+" and Vaccination ID: "+vaccinationID+" not found");
        }
        else
            vaccinationRepository.deleteById(compositeKey);
    }

    public Vaccination addVaccination(Vaccination vaccination) {
        return vaccinationRepository.save(vaccination);
    }
}
