package com.example.core.models;

public class RequestBuilder {
    private String id;
    private String action;
    private String requestTable;
    private String requester;
    private String requesterID;
    private String requestType;
    private String requestedAccessPeriod;
    private String practitionerID;
    private String patientID;
    private Vaccination vaccination;
    private Patient patient;
    private Practitioner practitioner;

    public RequestBuilder setID(String id) {
        this.id = id;
        return this;
    }
    public RequestBuilder setAction(String action) {
        this.action = action;
        return this;
    }

    public RequestBuilder setRequestTable(String requestTable) {
        this.requestTable = requestTable;
        return this;
    }

    public RequestBuilder setRequester(String requester) {
        this.requester = requester;
        return this;
    }

    public RequestBuilder setRequesterID(String requesterID) {
        this.requesterID = requesterID;
        return this;
    }

    public RequestBuilder setRequestType(String requestType) {
        this.requestType = requestType;
        return this;
    }

    public RequestBuilder setRequestedAccessPeriod(String requestedAccessPeriod) {
        this.requestedAccessPeriod = requestedAccessPeriod;
        return this;
    }

    public RequestBuilder setPractitionerID(String practitionerID) {
        this.practitionerID = practitionerID;
        return this;
    }

    public RequestBuilder setPatientID(String patientID) {
        this.patientID = patientID;
        return this;
    }

    public RequestBuilder setVaccination(Vaccination vaccination) {
        this.vaccination = vaccination;
        return this;
    }

    public RequestBuilder setPatient(Patient patient) {
        this.patient = patient;
        return this;
    }

    public RequestBuilder setPractitioner(Practitioner practitioner) {
        this.practitioner = practitioner;
        return this;
    }

    public Request createRequest() {
        return new Request(id,action, requestTable, requester, requesterID, requestType, requestedAccessPeriod, practitionerID, patientID, vaccination, patient, practitioner);
    }
}