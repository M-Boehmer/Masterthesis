package com.example.core.repository;

import com.example.core.models.Clearance;
import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@EnableScan
@Repository
public interface ClearanceRepository extends CrudRepository<Clearance, Clearance.CompositeKey> {
    List<Clearance> findAllByPatientID(String patientID);
}
