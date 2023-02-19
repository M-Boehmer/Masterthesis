package com.example.core.services;

import com.example.core.models.Practitioner;
import com.example.core.repository.PractitionerRepository;
import javax.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PractitionerService {

    @Autowired
    PractitionerRepository practitionerRepository;

    public Optional<Practitioner> getPractitioner(String id) {
        return practitionerRepository.findById(id);
    }
    public List<Practitioner> getAllPractitioner(){
        return (List<Practitioner>) practitionerRepository.findAll();
    }
    public Practitioner updatePractitioner(Practitioner practitioner, String id) {
        boolean exists = practitionerRepository.existsById(id);
        if(!exists) {
            throw new EntityNotFoundException("Practitioner(id- " + id + ") Not Found !!");
        }
        else
            practitioner.setPractitionerID(id);
        return practitionerRepository.save(practitioner);
    }

    public void deletePractitioner(String id) {
        boolean exists = practitionerRepository.existsById(id);
        if(!exists) {
            throw new EntityNotFoundException("Practitioner(id- " + id + ") Not Found !!");
        }
        else
            practitionerRepository.deleteById(id);
    }

    public Practitioner addPractitioner(Practitioner practitioner) {
        return practitionerRepository.save(practitioner);
    }
}
