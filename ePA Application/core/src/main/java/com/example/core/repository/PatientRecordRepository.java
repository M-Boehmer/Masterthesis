package com.example.core.repository;

import com.example.core.models.PatientRecord;
import com.example.core.models.Vaccination;
import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@EnableScan
@Repository
public interface PatientRecordRepository extends CrudRepository<PatientRecord, PatientRecord.CompositeKey> {
    List<PatientRecord> queryAllByPractitionerID(String practitionerID);
}
