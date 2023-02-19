package com.example.core.models;


import com.amazonaws.services.dynamodbv2.datamodeling.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;

@AllArgsConstructor
@NoArgsConstructor
@DynamoDBTable(tableName = "Patient_record")
public class PatientRecord {

    @Id
    @DynamoDBIgnore
    private CompositeKey compositeKey;

    private String patientSince;


    @DynamoDBHashKey(attributeName = "PractitionerID")
    public String getPractitionerID() {return compositeKey != null ? compositeKey.getPractitionerID() : null;}
    public void setPractitionerID(String practitionerID) {
        if (compositeKey == null) {
            compositeKey = new CompositeKey();
    }
        compositeKey.setPractitionerID(practitionerID);}

    @DynamoDBRangeKey(attributeName = "PatientID")
    public String getPatientID() {return compositeKey != null ? compositeKey.getPatientID() : null;}
    public void setPatientID(String patientID) {
        if (compositeKey == null) {
            compositeKey = new CompositeKey();
        }
        compositeKey.setPatientID(patientID);
    }

    @DynamoDBAttribute(attributeName = "Patient since")
    public String getPatientSince() {return patientSince;}
    public void setPatientSince(String patientSince) {this.patientSince = patientSince;}

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @DynamoDBDocument
    public static class CompositeKey {
        @DynamoDBHashKey
        @DynamoDBAttribute(attributeName = "PractitionerID")
        private String practitionerID;

        @DynamoDBRangeKey
        @DynamoDBAttribute(attributeName = "PatientID")
        private String patientID;
    }


}

