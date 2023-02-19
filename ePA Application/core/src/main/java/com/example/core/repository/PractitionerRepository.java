package com.example.core.repository;


import com.example.core.models.Practitioner;

import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@EnableScan
@Repository
public interface PractitionerRepository extends CrudRepository<Practitioner,String> { }
