package com.example.core.repository;

import com.example.core.models.Vaccination;
import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@EnableScan
@Repository
public interface VaccinationRepository extends CrudRepository<Vaccination,Vaccination.CompositeKey> {
    List<Vaccination> queryAllByPatientID(String patientID);
}
