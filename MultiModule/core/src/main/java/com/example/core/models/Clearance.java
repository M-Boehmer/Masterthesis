package com.example.core.models;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import lombok.*;
import org.springframework.data.annotation.Id;

import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@RequiredArgsConstructor
@DynamoDBTable(tableName = "Clearance")
public class Clearance {

    @Id
    @DynamoDBIgnore
    @NonNull
    private CompositeKey compositeKey;

    private Map<String,String> clearanceMap;

    @DynamoDBHashKey(attributeName = "PatientID")
    public String getPatientID() {return compositeKey != null ? compositeKey.getPatientID() : null;}
    public void setPatientID(String patientID) {
        if (compositeKey == null) {
            compositeKey = new CompositeKey();
        }
        compositeKey.setPatientID(patientID);
    }
    @DynamoDBRangeKey(attributeName = "PractitionerID")
    public String getPractitionerID() {return compositeKey != null ? compositeKey.getPractitionerID() : null;}
    public void setPractitionerID(String practitionerID) {
        if (compositeKey == null) {
            compositeKey = new CompositeKey();
        }
        compositeKey.setPractitionerID(practitionerID);}
    @DynamoDBAttribute(attributeName = "ClearanceMap")
    public Map<String, String> getClearanceMap() {return clearanceMap;}
    public void setClearanceMap(Map<String, String> clearanceMap) {this.clearanceMap = clearanceMap;}

    /*
        @DynamoDBAttribute(attributeName = "Clearance")
        public String getClearance() {return clearance;}
        public void setClearance(String clearance) {this.clearance = clearance;}

        @DynamoDBAttribute(attributeName = "Timeframe")
        public String getTimeframe() {return timeframe;}
        public void setTimeframe(String timeframe) {this.timeframe = timeframe;}
        */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @DynamoDBDocument
    public static class CompositeKey {
        @DynamoDBHashKey
        @DynamoDBAttribute(attributeName = "PatientID")
        private String patientID;

        @DynamoDBRangeKey
        @DynamoDBAttribute(attributeName = "PractitionerID")
        private String practitionerID;
    }
}

