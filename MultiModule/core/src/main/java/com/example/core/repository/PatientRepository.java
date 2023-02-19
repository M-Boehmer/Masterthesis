package com.example.core.repository;

import com.example.core.models.Patient;
import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@EnableScan
@Repository
public interface PatientRepository extends CrudRepository<Patient, String> {
    List<Patient> getPatientsByNameAndSurname(String Name,String Surname);
}
