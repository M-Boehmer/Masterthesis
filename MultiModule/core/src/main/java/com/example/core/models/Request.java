package com.example.core.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Request {

    @JsonProperty("id")
    private String id;
    @JsonProperty("action")
    private String action; //CRUD for all except Clearance
    @JsonProperty("requestTable")
    private String requestTable; //CRUD for all except Clearance

    @JsonProperty("requester")
    private String requester; //all
    @JsonProperty("requesterID")
    private String requesterID; //all
    @JsonProperty("requestType")
    private String requestType; //all

    @JsonProperty("requestedAccessPeriod")
    private String requestedAccessPeriod; //Clearance

    @JsonProperty("practitionerID")
    private String practitionerID;
    @JsonProperty("patientID")
    private String patientID;

    @JsonProperty("vaccination")
    private Vaccination vaccination;
    @JsonProperty("patient")
    private Patient patient;
    @JsonProperty("practitioner")
    private Practitioner practitioner;

}
